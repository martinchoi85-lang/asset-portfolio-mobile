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
import com.choi.assetportfolio.domain.model.DailySnapshot
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TrendData(val day: String, val value: Float)
data class BestAssetData(val assetName: String, val returnRate: Double, val periodLabel: String)

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val data: List<DashboardAsset>,
        val totalAssetAmount: Double,
        val overallReturnRate: Double,
        val allocations: List<AllocationResult>,
        val trendData: List<TrendData> = emptyList(),
        val bestPerformingAsset: BestAssetData? = null
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

    private val _tabs = MutableStateFlow<List<String>>(listOf("전체 계좌", "+계좌추가"))
    val tabs: StateFlow<List<String>> = _tabs.asStateFlow()

    private var accountIdMap = mapOf<Int, String>()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isPrivacyModeEnabled = MutableStateFlow(false)
    val isPrivacyModeEnabled: StateFlow<Boolean> = _isPrivacyModeEnabled.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private var allDashboardAssets: List<DashboardAsset> = emptyList()
    private var allAllocations: List<AllocationResult> = emptyList()
    private var overallTwrReturnRate: Double = 0.0

    private val _selectedRange = MutableStateFlow("1주")
    val selectedRange: StateFlow<String> = _selectedRange.asStateFlow()

    private var allDailySnapshots: List<DailySnapshot> = emptyList()

    init {
        fetchDashboardData()
    }

    fun selectRange(range: String) {
        _selectedRange.value = range
        applyFilter()
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
        val filteredAssets = if (index == 0 || index == _tabs.value.lastIndex) {
            allDashboardAssets
        } else {
            val targetAccountId = accountIdMap[index]
            allDashboardAssets.filter { it.accountId == targetAccountId }
        }
        
        val totalValuation = filteredAssets.sumOf { it.totalValuationAmount }
        AppLogger.d("Assets filtered", data = "Selected Index: $index, Filtered Assets Count: ${filteredAssets.size}")

        val rangeDays = when (_selectedRange.value) {
            "1주" -> 7
            "1개월" -> 30
            "3개월" -> 90
            "6개월" -> 180
            "1년" -> 365
            else -> 7
        }
        val targetStartDate = LocalDate.now().minusDays(rangeDays.toLong())
        val snapshotsInRange = allDailySnapshots.filter { !it.date.isBefore(targetStartDate) }

        val trendMap = snapshotsInRange.groupBy { it.date }.mapValues { entry ->
            entry.value.sumOf { it.valuationAmount }
        }
        val sortedDates = trendMap.keys.sorted()
        val trendDataList = sortedDates.map { date ->
            TrendData(
                day = date.format(DateTimeFormatter.ofPattern("MM/dd")),
                value = trendMap[date]!!.toFloat()
            )
        }

        var bestAsset: BestAssetData? = null
        if (snapshotsInRange.isNotEmpty()) {
            val assetPerformance = snapshotsInRange.groupBy { it.assetId }.mapNotNull { (assetId, snaps) ->
                val startSnap = snaps.minByOrNull { it.date }
                val endSnap = snaps.maxByOrNull { it.date }
                if (startSnap != null && endSnap != null && startSnap.valuationAmount > 0) {
                    val rate = (endSnap.valuationAmount - startSnap.valuationAmount) / startSnap.valuationAmount * 100
                    val asset = allDashboardAssets.find { it.assetId == assetId }
                    asset?.let { it.nameKr to rate }
                } else null
            }
            val best = assetPerformance.maxByOrNull { it.second }
            if (best != null) {
                bestAsset = BestAssetData(
                    assetName = best.first,
                    returnRate = best.second,
                    periodLabel = _selectedRange.value
                )
            }
        }

        AppLogger.d("Asset Trend & Insight", data = "Trend Data Points: ${trendDataList.size}, Best Asset: ${bestAsset?.assetName} (${bestAsset?.returnRate}%)")

        if (filteredAssets.isEmpty()) {
            _uiState.value = DashboardUiState.Empty
        } else {
            _uiState.value = DashboardUiState.Success(
                data = filteredAssets,
                totalAssetAmount = totalValuation,
                overallReturnRate = overallTwrReturnRate,
                allocations = allAllocations,
                trendData = trendDataList,
                bestPerformingAsset = bestAsset
            )
        }
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            AppLogger.d("State updated to Loading")
            
            try {
                // Fetch user accounts to dynamically build tabs
                val userAccounts = assetRepository.fetchUserAccounts()
                val newTabs = mutableListOf("전체 계좌")
                val newAccountIdMap = mutableMapOf<Int, String>()
                
                userAccounts.forEachIndexed { idx, account ->
                    newTabs.add(account.name)
                    newAccountIdMap[idx + 1] = account.id
                }
                newTabs.add("+계좌추가")
                
                _tabs.value = newTabs
                accountIdMap = newAccountIdMap

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

                    // 3. 최근 1년치 Daily Snapshots 로드
                    val endDate = LocalDate.now()
                    val startDate = endDate.minusYears(1)
                    allDailySnapshots = portfolioRepository.getDailySnapshots(startDate, endDate)

                    // 4. 최종 가공된 데이터를 상태에 캐싱 후 UI 전달
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
