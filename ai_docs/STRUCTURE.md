# 🏛️ Project Structure & API Map

*Generated automatically on: 2026-07-06 22:13:37*

## 1. Directory & File Tree

```text
. (root: asset-portfolio-mobile)
├── GEMINI.md
├── account_detail_sample.kt
├── ai_docs/
│   ├── ADR.md
│   ├── DB_SCHEMA.md
│   ├── DESIGN.md
│   ├── PRD.md
│   ├── ROADMAP.md
│   ├── STRUCTURE.md
│   ├── active_state.md
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   ├── src/
│   │   ├── androidTest/
│   │   │   ├── java/
│   │   │   │   ├── com/
│   │   │   │   │   ├── choi/
│   │   │   │   │   │   ├── assetportfolio/
│   │   │   │   │   │   │   ├── ExampleInstrumentedTest.kt
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/
│   │   │   │   ├── com/
│   │   │   │   │   ├── choi/
│   │   │   │   │   │   ├── assetportfolio/
│   │   │   │   │   │   │   ├── AssetApplication.kt
│   │   │   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   │   │   ├── core/
│   │   │   │   │   │   │   │   ├── network/
│   │   │   │   │   │   │   │   ├── serialization/
│   │   │   │   │   │   │   │   │   ├── LocalDateSerializer.kt
│   │   │   │   │   │   │   │   │   ├── ZonedDateTimeSerializer.kt
│   │   │   │   │   │   │   │   ├── session/
│   │   │   │   │   │   │   │   │   ├── SessionManager.kt
│   │   │   │   │   │   │   │   ├── util/
│   │   │   │   │   │   │   │   │   ├── AppLogger.kt
│   │   │   │   │   │   │   ├── data/
│   │   │   │   │   │   │   │   ├── local/
│   │   │   │   │   │   │   │   │   ├── LocalTransactionEntity.kt
│   │   │   │   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   │   │   │   │   ├── LocalTransactionEntity.kt
│   │   │   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   │   │   │   ├── AssetRepositoryImpl.kt
│   │   │   │   │   │   │   │   │   ├── ManualAssetRepository.kt
│   │   │   │   │   │   │   │   │   ├── PortfolioRepository.kt
│   │   │   │   │   │   │   │   │   ├── SyncRepository.kt
│   │   │   │   │   │   │   ├── domain/
│   │   │   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   │   │   ├── Account.kt
│   │   │   │   │   │   │   │   │   ├── Asset.kt
│   │   │   │   │   │   │   │   │   ├── AssetSegment.kt
│   │   │   │   │   │   │   │   │   ├── DailySnapshot.kt
│   │   │   │   │   │   │   │   │   ├── DashboardAsset.kt
│   │   │   │   │   │   │   │   │   ├── PortfolioTargetWeight.kt
│   │   │   │   │   │   │   │   │   ├── Transaction.kt
│   │   │   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   │   │   │   ├── AssetRepository.kt
│   │   │   │   │   │   │   │   ├── usecase/
│   │   │   │   │   │   │   │   │   ├── CalculateLookthroughUseCase.kt
│   │   │   │   │   │   │   │   │   ├── CalculatePortfolioYieldUseCase.kt
│   │   │   │   │   │   │   │   │   ├── CurrencyConverter.kt
│   │   │   │   │   │   │   │   │   ├── GetLookthroughAllocationUseCase.kt
│   │   │   │   │   │   │   ├── ui/
│   │   │   │   │   │   │   │   ├── analysis/
│   │   │   │   │   │   │   │   │   ├── AnalysisScreen.kt
│   │   │   │   │   │   │   │   │   ├── AnalysisViewModel.kt
│   │   │   │   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   │   │   │   ├── DashboardScreen.kt
│   │   │   │   │   │   │   │   │   ├── FinancialDashboardViewModel.kt
│   │   │   │   │   │   │   │   ├── main/
│   │   │   │   │   │   │   │   │   ├── MainContainerScreen.kt
│   │   │   │   │   │   │   │   ├── management/
│   │   │   │   │   │   │   │   │   ├── ManagementHubScreen.kt
│   │   │   │   │   │   │   │   │   ├── ManagementHubViewModel.kt
│   │   │   │   │   │   │   │   ├── navigation/
│   │   │   │   │   │   │   │   │   ├── Screen.kt
│   │   │   │   │   │   │   │   ├── theme/
│   │   │   │   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   │   │   │   ├── Type.kt
│   │   │   │   │   │   │   │   ├── transactions/
│   │   │   │   │   │   │   │   │   ├── TransactionsScreen.kt
│   │   │   │   │   │   │   │   │   ├── TransactionsViewModel.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   ├── ic_launcher_round.xml
│   │   │   │   ├── mipmap-hdpi/
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── mipmap-mdpi/
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── mipmap-xhdpi/
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── mipmap-xxhdpi/
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── mipmap-xxxhdpi/
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   ├── ic_launcher_round.webp
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── themes.xml
│   │   │   │   ├── xml/
│   │   │   │   │   ├── backup_rules.xml
│   │   │   │   │   ├── data_extraction_rules.xml
│   │   ├── test/
│   │   │   ├── java/
│   │   │   │   ├── com/
│   │   │   │   │   ├── choi/
│   │   │   │   │   │   ├── assetportfolio/
│   │   │   │   │   │   │   ├── ExampleUnitTest.kt
├── build.gradle.kts
├── dashboard_sample.kt
├── scripts/
├── settings.gradle.kts
```

## 2. Source Files & Function Lists

### 📄 `account_detail_sample.kt`
- `fun AccountDetailScreen()`
- `fun ProfitPerformanceCardView()`
- `fun AssetAllocationBarCardView()`
- `fun SectorMetricView()`
- `fun ActiveHoldingItemView()`
- `fun formatAmountDouble()`
- `fun AccountDetailPreview()`

### 📄 `app/src/androidTest/java/com/choi/assetportfolio/ExampleInstrumentedTest.kt`
- `fun useAppContext()`

### 📄 `app/src/main/java/com/choi/assetportfolio/AssetApplication.kt`
- `fun onCreate()`

### 📄 `app/src/main/java/com/choi/assetportfolio/MainActivity.kt`
- `fun onCreate()`

### 📄 `app/src/main/java/com/choi/assetportfolio/core/serialization/LocalDateSerializer.kt`
- `fun serialize()`
- `fun deserialize()`

### 📄 `app/src/main/java/com/choi/assetportfolio/core/serialization/ZonedDateTimeSerializer.kt`
- `fun serialize()`
- `fun deserialize()`

### 📄 `app/src/main/java/com/choi/assetportfolio/core/session/SessionManager.kt`
- `fun setUserId()`
- `fun clearSession()`
- `fun requireUserId()`

### 📄 `app/src/main/java/com/choi/assetportfolio/core/util/AppLogger.kt`
- `fun d()`
- `fun e()`

### 📄 `app/src/main/java/com/choi/assetportfolio/data/repository/AssetRepositoryImpl.kt`
- `fun invalidateCache()`
- `fun fetchUserAccounts()`
- `fun fetchUserAssets()`
- `fun fetchDashboardAssets()`
- `fun getAssetSegments()`

### 📄 `app/src/main/java/com/choi/assetportfolio/data/repository/ManualAssetRepository.kt`
- `fun getCurrentCostBasis()`
- `fun getCostBasisEvents()`
- `fun updateManualAsset()`

### 📄 `app/src/main/java/com/choi/assetportfolio/data/repository/PortfolioRepository.kt`
- `fun getTransactions()`
- `fun getUserAccounts()`
- `fun getTargetWeights()`
- `fun getDailySnapshots()`

### 📄 `app/src/main/java/com/choi/assetportfolio/data/repository/SyncRepository.kt`
- `fun setCacheDirty()`
- `fun syncOnForegroundIfNeeded()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/model/Transaction.kt`
- `fun generateHashKey()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/repository/AssetRepository.kt`
- `fun fetchUserAccounts()`
- `fun fetchUserAssets()`
- `fun fetchDashboardAssets()`
- `fun getAssetSegments()`
- `fun invalidateCache()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/usecase/CalculateLookthroughUseCase.kt`
- `fun execute()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/usecase/CalculatePortfolioYieldUseCase.kt`
- `fun invoke()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/usecase/CurrencyConverter.kt`
- `fun convertToKrw()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/usecase/GetLookthroughAllocationUseCase.kt`
- `fun invoke()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/analysis/AnalysisScreen.kt`
- `fun AnalysisScreen()`
- `fun LookthroughAllocationView()`
- `fun RebalancingGuideView()`
- `fun mapAssetClassToKorean()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/analysis/AnalysisViewModel.kt`
- `fun loadAnalysisData()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/dashboard/DashboardScreen.kt`
- `fun DashboardScreen()`
- `fun mapAllocationResultToData()`
- `fun DashboardContent()`
- `fun PortfolioAllocationCardView()`
- `fun ProfitPerformanceCardView()`
- `fun AssetAllocationBarCardView()`
- `fun SectorMetricView()`
- `fun ActiveHoldingItemView()`
- `fun AssetTrendCardView()`
- `fun PerformanceInsightCardView()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/dashboard/FinancialDashboardViewModel.kt`
- `fun selectRange()`
- `fun togglePrivacyMode()`
- `fun toggleCurrencyDisplay()`
- `fun selectTab()`
- `fun fetchDashboardData()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/main/MainContainerScreen.kt`
- `fun MainContainerScreen()`
- `fun PlaceholderScreen()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/management/ManagementHubScreen.kt`
- `fun ManagementHubScreen()`
- `fun EventHistoryItem()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/management/ManagementHubViewModel.kt`
- `fun loadData()`
- `fun saveManualAssetUpdate()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/theme/Theme.kt`
- `fun AssetPortfolioMobileTheme()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/transactions/TransactionsScreen.kt`
- `fun TransactionsScreen()`
- `fun StagingTransactionItem()`
- `fun ConfirmedTransactionItem()`
- `fun TradeTypeBadge()`
- `fun formatPrice()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/transactions/TransactionsViewModel.kt`
- `fun fetchData()`
- `fun selectAccount()`
- `fun selectPeriod()`
- `fun applyFilters()`
- `fun addTransactionToStaging()`
- `fun approveStaging()`

### 📄 `app/src/test/java/com/choi/assetportfolio/ExampleUnitTest.kt`
- `fun addition_isCorrect()`

### 📄 `dashboard_sample.kt`
- `fun PortfolioDashboardScreen()`
- `fun AssetTrendCardView()`
- `fun PerformanceInsightCardView()`
- `fun RiskProfileCardView()`
- `fun PortfolioAllocationCardView()`
- `fun AddWidgetCardView()`
- `fun BottomNavigationBar()`
- `fun formatAmount()`
- `fun PortfolioDashboardPreview()`
