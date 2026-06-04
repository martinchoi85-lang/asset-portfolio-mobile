package com.choi.assetportfolio.ui.dashboard

import com.choi.assetportfolio.core.util.AppLogger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.choi.assetportfolio.domain.model.DashboardAsset
import com.choi.assetportfolio.domain.model.AssetSegment
import com.choi.assetportfolio.domain.model.Transaction
import com.choi.assetportfolio.domain.repository.AssetRepository
import com.choi.assetportfolio.data.repository.PortfolioRepository
import com.choi.assetportfolio.domain.usecase.CalculatePortfolioYieldUseCase
import com.choi.assetportfolio.domain.usecase.GetLookthroughAllocationUseCase
import com.choi.assetportfolio.domain.usecase.AllocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val data: List<DashboardAsset>,
        val totalAssetAmount: Double,
        val overallReturnRate: Double,
        val allocations: List<AllocationResult>
    ) : DashboardUiState
    object Empty : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class FinancialDashboardViewModel(
    private val assetRepository: AssetRepository,
    private val portfolioRepository: PortfolioRepository,
    private val calculatePortfolioYieldUseCase: CalculatePortfolioYieldUseCase,
    private val getLookthroughAllocationUseCase: GetLookthroughAllocationUseCase
) : ViewModel() {

    val tabs = listOf("전체 계좌", "국민은행", "미래에셋", "신한은행", "+계좌추가")
    private val accountIdMap = mapOf(
        1 to "kb_bank_id",
        2 to "mirae_asset_id",
        3 to "shinhan_bank_id"
    )

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isPrivacyModeEnabled = MutableStateFlow(true)
    val isPrivacyModeEnabled: StateFlow<Boolean> = _isPrivacyModeEnabled.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private var allDashboardAssets: List<DashboardAsset> = emptyList()
    private var allAllocations: List<AllocationResult> = emptyList()
    private var overallTwrReturnRate: Double = 0.0

    init {
        fetchDashboardData()
    }

    fun togglePrivacyMode() {
        _isPrivacyModeEnabled.value = !_isPrivacyModeEnabled.value
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        val targetAccountId = accountIdMap[index]
        AppLogger.d("Tab selected", data = "Index=$index, targetAccountId=$targetAccountId")
        applyFilter()
    }

    private fun applyFilter() {
        val index = _selectedTabIndex.value
        val filteredAssets = if (index == 0 || index == 4) {
            allDashboardAssets
        } else {
            val targetAccountId = accountIdMap[index]
            allDashboardAssets.filter { it.accountId == targetAccountId }
        }
        
        val totalValuation = filteredAssets.sumOf { it.totalValuationAmount }
        AppLogger.d("Assets filtered", data = "Selected Index: $index, Filtered Assets Count: ${filteredAssets.size}")

        if (filteredAssets.isEmpty()) {
            _uiState.value = DashboardUiState.Empty
        } else {
            _uiState.value = DashboardUiState.Success(
                data = filteredAssets,
                totalAssetAmount = totalValuation,
                overallReturnRate = overallTwrReturnRate,
                allocations = allAllocations
            )
        }
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            AppLogger.d("State updated to Loading")
            
            try {
                val dashboardAssets = assetRepository.fetchDashboardAssets()
                AppLogger.d("Data fetched from Repository", data = dashboardAssets.size.toString() + " assets")
                
                if (dashboardAssets.isEmpty()) {
                    _uiState.value = DashboardUiState.Empty
                    AppLogger.d("State updated to Empty")
                } else {
                    val totalValuation = dashboardAssets.sumOf { it.totalValuationAmount }
                    
                    // 1. 거래 내역 로드 및 TWR 수익률 계산
                    val allTransactions = mutableListOf<Transaction>()
                    var page = 0
                    while (true) {
                        val batch = portfolioRepository.getTransactions(page = page, pageSize = 500)
                        allTransactions.addAll(batch)
                        if (batch.size < 500) break
                        page++
                    }
                    val yieldResult = calculatePortfolioYieldUseCase(allTransactions, totalValuation)

                    // 2. 자산 상세 정보 및 세그먼트 로드하여 Look-through 비중 분석
                    val userAssets = assetRepository.fetchUserAssets()
                    val allSegments = mutableListOf<AssetSegment>()
                    for (asset in dashboardAssets.filter { it.lookthroughAvailable }) {
                        allSegments.addAll(assetRepository.getAssetSegments(asset.assetId))
                    }
                    val allocations = getLookthroughAllocationUseCase(dashboardAssets, userAssets, allSegments)

                    // 3. 최종 가공된 데이터를 상태에 캐싱 후 UI 전달
                    allDashboardAssets = dashboardAssets
                    overallTwrReturnRate = yieldResult.timeWeightedReturn
                    allAllocations = allocations
                    
                    AppLogger.d("All data loaded. Applying filter for tab: ${_selectedTabIndex.value}")
                    applyFilter()
                }
            } catch (e: Exception) {
                AppLogger.e("State updated to Error", error = e)
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
