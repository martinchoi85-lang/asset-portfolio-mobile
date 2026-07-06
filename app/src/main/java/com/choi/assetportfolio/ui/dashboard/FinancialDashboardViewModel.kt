package com.choi.assetportfolio.ui.dashboard

import com.choi.assetportfolio.core.util.AppLogger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.choi.assetportfolio.domain.model.DashboardAsset
import com.choi.assetportfolio.domain.model.Asset
import com.choi.assetportfolio.domain.model.AssetSegment
import com.choi.assetportfolio.domain.model.Transaction
import com.choi.assetportfolio.domain.model.Account
import com.choi.assetportfolio.domain.repository.AssetRepository
import com.choi.assetportfolio.data.repository.PortfolioRepository
import com.choi.assetportfolio.domain.usecase.CalculatePortfolioYieldUseCase
import com.choi.assetportfolio.domain.usecase.GetLookthroughAllocationUseCase
import com.choi.assetportfolio.domain.usecase.AllocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.choi.assetportfolio.domain.model.DailySnapshot
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TrendData(val day: String, val value: Float, val timestamp: Long = 0L)
data class BestAssetData(val assetName: String, val returnRate: Double, val periodLabel: String)

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val data: List<DashboardAsset>,
        val totalAssetAmount: Double,
        val overallReturnRate: Double,
        val allocations: List<AllocationResult>,
        val trendData: List<TrendData> = emptyList(),
        val bestPerformingAsset: BestAssetData? = null,
        val hasUsdAccount: Boolean = false,
        val isUsdDisplayPreferred: Boolean = false
    ) : DashboardUiState
    object Empty : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

data class RawPortfolioData(
    val accounts: List<Account> = emptyList(),
    val dashboardAssets: List<DashboardAsset> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val dailySnapshots: List<DailySnapshot> = emptyList(),
    val userAssets: List<Asset> = emptyList(),
    val allSegments: List<AssetSegment> = emptyList()
)

class FinancialDashboardViewModel(
    private val assetRepository: AssetRepository,
    private val portfolioRepository: PortfolioRepository,
    private val calculatePortfolioYieldUseCase: CalculatePortfolioYieldUseCase,
    private val getLookthroughAllocationUseCase: GetLookthroughAllocationUseCase
) : ViewModel() {

    private val _tabs = MutableStateFlow<List<String>>(listOf("전체 계좌", "+계좌추가"))
    val tabs: StateFlow<List<String>> = _tabs.asStateFlow()

    private val _isPrivacyModeEnabled = MutableStateFlow(false)
    val isPrivacyModeEnabled: StateFlow<Boolean> = _isPrivacyModeEnabled.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _selectedRange = MutableStateFlow("1M")
    val selectedRange: StateFlow<String> = _selectedRange.asStateFlow()
    
    private val _isUsdDisplayPreferred = MutableStateFlow(false)
    
    private val rawDataFlow = MutableStateFlow(RawPortfolioData())
    
    private val exchangeRateFlow = MutableStateFlow(1380.0)

    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedTabIndex,
        _selectedRange,
        _isUsdDisplayPreferred,
        rawDataFlow,
        exchangeRateFlow
    ) { tabIndex, range, isUsdPref, rawData, exchangeRate ->
        if (rawData.dashboardAssets.isEmpty()) {
            return@combine DashboardUiState.Empty
        }

        val targetAccountId = if (tabIndex == 0 || tabIndex == _tabs.value.lastIndex) null else {
            rawData.accounts.getOrNull(tabIndex - 1)?.id
        }
        
        val activeAccount = rawData.accounts.find { it.id == targetAccountId }
        val hasUsdAccount = activeAccount?.currency == "USD"
        val applyUsdConversion = hasUsdAccount && isUsdPref

        val filteredAssets = if (targetAccountId == null) {
            rawData.dashboardAssets
        } else {
            rawData.dashboardAssets.filter { it.accountId == targetAccountId }
        }

        var totalValuation = filteredAssets.sumOf { it.totalValuationAmount }
        if (applyUsdConversion) {
            totalValuation /= exchangeRate
        }

        val filteredTransactions = if (targetAccountId == null) {
            rawData.transactions.filter { it.parentTransactionId == null }
        } else {
            rawData.transactions.filter { it.accountId == targetAccountId }
        }

        val rangeDays = when (range) {
            "1W" -> 7
            "1M" -> 30
            "3M" -> 90
            "6M" -> 180
            "1Y" -> 365
            else -> 36500 // ALL
        }
        val targetStartDate = LocalDate.now().minusDays(rangeDays.toLong())

        // Construct chart state using ONLY available discrete data points
        val trendDataMap = mutableMapOf<LocalDate, Double>()
        val now = LocalDate.now()

        for (asset in filteredAssets) {
            val snapshots = rawData.dailySnapshots.filter { 
                it.assetId == asset.assetId && !it.date.isBefore(targetStartDate) && !it.date.isAfter(now)
            }
            for (snapshot in snapshots) {
                trendDataMap[snapshot.date] = trendDataMap.getOrDefault(snapshot.date, 0.0) + snapshot.valuationAmount
            }
        }

        val slicedTrendData = trendDataMap.map { (date, valuation) ->
            var finalValuation = valuation
            if (applyUsdConversion) {
                finalValuation /= exchangeRate
            }
            val epoch = date.atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond()
            TrendData(
                day = date.format(DateTimeFormatter.ofPattern("MM/dd")),
                value = finalValuation.toFloat(),
                timestamp = epoch
            )
        }.sortedBy { it.timestamp }

        var bestAssetObj: DashboardAsset? = null
        var maxDelta = Double.NEGATIVE_INFINITY
        
        for (asset in filteredAssets) {
            val startSnapshot = rawData.dailySnapshots
                .filter { it.assetId == asset.assetId && !it.date.isBefore(targetStartDate) }
                .minByOrNull { it.date }
            val endSnapshot = rawData.dailySnapshots
                .filter { it.assetId == asset.assetId && !it.date.isAfter(now) }
                .maxByOrNull { it.date }
            if (startSnapshot != null && endSnapshot != null && startSnapshot.valuationAmount > 0) {
                val delta = (endSnapshot.valuationAmount - startSnapshot.valuationAmount) / startSnapshot.valuationAmount * 100.0
                if (delta != 0.0 && delta > maxDelta) {
                    maxDelta = delta
                    bestAssetObj = asset
                }
            }
        }
        
        val finalBestAssetObj = bestAssetObj ?: filteredAssets.maxByOrNull { it.unrealizedReturnRate }
        val bestAsset = finalBestAssetObj?.let {
            val isLifetime = bestAssetObj == null
            BestAssetData(
                assetName = it.nameKr,
                returnRate = if (isLifetime) it.unrealizedReturnRate else maxDelta,
                periodLabel = if (isLifetime) "ALL" else range
            )
        }

        val yieldResult = calculatePortfolioYieldUseCase(filteredTransactions, totalValuation)
        val allocations = getLookthroughAllocationUseCase(filteredAssets, rawData.userAssets, rawData.allSegments)

        AppLogger.d("Asset Trend & Insight", data = "Trend List Count: ${slicedTrendData.size}, Yield: ${yieldResult.timeWeightedReturn}")

        DashboardUiState.Success(
            data = filteredAssets,
            totalAssetAmount = totalValuation,
            overallReturnRate = yieldResult.timeWeightedReturn,
            allocations = allocations,
            trendData = slicedTrendData,
            bestPerformingAsset = bestAsset,
            hasUsdAccount = hasUsdAccount,
            isUsdDisplayPreferred = applyUsdConversion
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    init {
        fetchDashboardData()
    }

    fun selectRange(range: String) {
        _selectedRange.value = range
    }

    fun togglePrivacyMode() {
        _isPrivacyModeEnabled.value = !_isPrivacyModeEnabled.value
    }

    fun toggleCurrencyDisplay() {
        _isUsdDisplayPreferred.value = !_isUsdDisplayPreferred.value
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            try {
                val userAccounts = assetRepository.fetchUserAccounts()
                val newTabs = mutableListOf("전체 계좌")
                userAccounts.forEach { account ->
                    newTabs.add(account.name)
                }
                newTabs.add("+계좌추가")
                _tabs.value = newTabs

                val dashboardAssets = assetRepository.fetchDashboardAssets()
                
                val fetchedTransactions = mutableListOf<Transaction>()
                var page = 0
                while (true) {
                    val batch = portfolioRepository.getTransactions(page = page, pageSize = 500)
                    fetchedTransactions.addAll(batch)
                    if (batch.size < 500) break
                    page++
                }

                val endDate = LocalDate.now()
                val startDate = endDate.minusYears(1)
                val allDailySnapshots = portfolioRepository.getDailySnapshots(startDate, endDate)
                
                val userAssets = assetRepository.fetchUserAssets()
                val lookthroughAssetIds = dashboardAssets.filter { it.lookthroughAvailable }.map { it.assetId }
                val allSegments = if (lookthroughAssetIds.isNotEmpty()) {
                    assetRepository.getAssetSegments(lookthroughAssetIds)
                } else emptyList()

                rawDataFlow.value = RawPortfolioData(
                    accounts = userAccounts,
                    dashboardAssets = dashboardAssets,
                    transactions = fetchedTransactions,
                    dailySnapshots = allDailySnapshots,
                    userAssets = userAssets,
                    allSegments = allSegments
                )
            } catch (e: Exception) {
                AppLogger.e("State updated to Error", error = e)
            }
        }
    }
}
