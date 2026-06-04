# 🏛️ Project Structure & API Map

*Generated automatically on: 2026-05-28 22:43:47*

## 1. Directory & File Tree

```text
. (root: asset-portfolio-mobile)
├── GEMINI.md
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
│   │   │   │   │   │   │   │   │   ├── ZonedDateTimeSerializer.kt
│   │   │   │   │   │   │   │   ├── session/
│   │   │   │   │   │   │   │   │   ├── SessionManager.kt
│   │   │   │   │   │   │   │   ├── util/
│   │   │   │   │   │   │   │   │   ├── AppLogger.kt
│   │   │   │   │   │   │   ├── data/
│   │   │   │   │   │   │   │   ├── local/
│   │   │   │   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   │   │   │   │   ├── LocalTransactionEntity.kt
│   │   │   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   │   │   │   ├── AssetRepositoryImpl.kt
│   │   │   │   │   │   │   │   │   ├── PortfolioRepository.kt
│   │   │   │   │   │   │   │   │   ├── SyncRepository.kt
│   │   │   │   │   │   │   ├── domain/
│   │   │   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   │   │   ├── Account.kt
│   │   │   │   │   │   │   │   │   ├── Asset.kt
│   │   │   │   │   │   │   │   │   ├── AssetSegment.kt
│   │   │   │   │   │   │   │   │   ├── DashboardAsset.kt
│   │   │   │   │   │   │   │   │   ├── Transaction.kt
│   │   │   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   │   │   │   ├── AssetRepository.kt
│   │   │   │   │   │   │   │   ├── usecase/
│   │   │   │   │   │   │   │   │   ├── CalculateLookthroughUseCase.kt
│   │   │   │   │   │   │   │   │   ├── CalculatePortfolioYieldUseCase.kt
│   │   │   │   │   │   │   │   │   ├── GetLookthroughAllocationUseCase.kt
│   │   │   │   │   │   │   ├── ui/
│   │   │   │   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   │   │   │   ├── DashboardScreen.kt
│   │   │   │   │   │   │   │   │   ├── FinancialDashboardViewModel.kt
│   │   │   │   │   │   │   │   ├── theme/
│   │   │   │   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   │   │   │   ├── Type.kt
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
├── docs/
│   ├── CORE_LOGIC.md
│   ├── DB_SCHEMA.md
│   ├── DESIGN.md
│   ├── PARSING_LEGACY.md
├── scripts/
│   ├── generate_structure.py
├── settings.gradle.kts
├── 작업_마무리
```

## 2. Source Files & Function Lists

### 📄 `app/src/androidTest/java/com/choi/assetportfolio/ExampleInstrumentedTest.kt`
- `fun useAppContext()`

### 📄 `app/src/main/java/com/choi/assetportfolio/AssetApplication.kt`
- `fun onCreate()`

### 📄 `app/src/main/java/com/choi/assetportfolio/MainActivity.kt`
- `fun onCreate()`

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
- `fun fetchUserAccounts()`
- `fun fetchUserAssets()`
- `fun fetchDashboardAssets()`
- `fun getAssetSegments()`
- `fun toDomain()`

### 📄 `app/src/main/java/com/choi/assetportfolio/data/repository/PortfolioRepository.kt`
- `fun getTransactions()`

### 📄 `app/src/main/java/com/choi/assetportfolio/data/repository/SyncRepository.kt`
- `fun syncTransactions()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/model/Transaction.kt`
- `fun generateHashKey()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/repository/AssetRepository.kt`
- `fun fetchUserAccounts()`
- `fun fetchUserAssets()`
- `fun fetchDashboardAssets()`
- `fun getAssetSegments()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/usecase/CalculateLookthroughUseCase.kt`
- `fun execute()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/usecase/CalculatePortfolioYieldUseCase.kt`
- `fun invoke()`

### 📄 `app/src/main/java/com/choi/assetportfolio/domain/usecase/GetLookthroughAllocationUseCase.kt`
- `fun invoke()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/dashboard/DashboardScreen.kt`
- `fun DashboardScreen()`
- `fun DashboardContent()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/dashboard/FinancialDashboardViewModel.kt`
- `fun togglePrivacyMode()`
- `fun selectTab()`
- `fun applyFilter()`
- `fun fetchDashboardData()`

### 📄 `app/src/main/java/com/choi/assetportfolio/ui/theme/Theme.kt`
- `fun AssetPortfolioMobileTheme()`

### 📄 `app/src/test/java/com/choi/assetportfolio/ExampleUnitTest.kt`
- `fun addition_isCorrect()`
