/**
 * 자산 분석(Analysis) 화면의 상태와 룩스루(Look-through) 로직을 조율하는 ViewModel.
 */
package com.choi.assetportfolio.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.choi.assetportfolio.core.util.AppLogger
import com.choi.assetportfolio.domain.repository.AssetRepository
import com.choi.assetportfolio.data.repository.PortfolioRepository
import com.choi.assetportfolio.domain.usecase.AllocationResult
import com.choi.assetportfolio.domain.usecase.GetLookthroughAllocationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AnalysisUiState {
    object Loading : AnalysisUiState
    object Empty : AnalysisUiState
    data class Success(
        val allocations: List<AllocationResult>,
        val rebalancingGuide: List<RebalancingItem>
    ) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

data class RebalancingItem(
    val category: String,
    val targetWeight: Double,
    val actualWeight: Double,
    val gap: Double // target - actual
)

class AnalysisViewModel(
    private val assetRepository: AssetRepository,
    private val portfolioRepository: PortfolioRepository,
    private val getLookthroughAllocationUseCase: GetLookthroughAllocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Loading)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadAnalysisData()
    }

    private fun loadAnalysisData() {
        viewModelScope.launch {
            _uiState.value = AnalysisUiState.Loading
            try {
                // 1. 자산 데이터 실시간 Fetch
                val dashboardAssets = assetRepository.fetchDashboardAssets()
                
                if (dashboardAssets.isEmpty()) {
                    AppLogger.d("Analysis 로드 - dashboardAssets가 비어있으므로 Empty 상태 전환")
                    _uiState.value = AnalysisUiState.Empty
                    return@launch
                }

                val assets = assetRepository.fetchUserAssets()
                
                // 모든 자산의 세그먼트를 묶어서 가져옵니다 (간이 구현, 실제론 성능을 위해 배치 필요)
                val allSegments = assets.flatMap { assetRepository.getAssetSegments(it.id) }

                // 2. 룩스루 연산 주입
                val allocations = getLookthroughAllocationUseCase(dashboardAssets, assets, allSegments)
                
                // 로깅 가이드에 맞게 출력
                val explodedCount = allocations.size
                AppLogger.d("Look-through Ingestion", data = "${dashboardAssets.size} items exploded into $explodedCount underlying classes")

                // 3. 목표 비중 가져오기
                val targets = portfolioRepository.getTargetWeights("underlying_asset_class")
                
                // 4. 리밸런싱 가이드 맵핑
                val rebalancingGuide = targets.map { target ->
                    val actual = allocations.find { it.assetClass == target.targetCategory }?.weight ?: 0.0
                    // DB의 target_weight는 % 단위일 수도 있고 비율 단위(0~1)일 수도 있음. 
                    // 여기서는 비율(0~1) 기준으로 가정하고 퍼센티지로 변환.
                    val actualPercent = actual * 100.0
                    RebalancingItem(
                        category = target.targetCategory,
                        targetWeight = target.targetWeight,
                        actualWeight = actualPercent,
                        gap = target.targetWeight - actualPercent
                    )
                }.sortedBy { it.gap }

                _uiState.value = AnalysisUiState.Success(allocations, rebalancingGuide)
            } catch (e: Exception) {
                AppLogger.e("Analysis 로드 실패", e)
                _uiState.value = AnalysisUiState.Error(e.message ?: "알 수 없는 에러 발생")
            }
        }
    }
}
