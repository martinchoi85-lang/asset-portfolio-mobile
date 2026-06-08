# 📍 ACTIVE_STATE: Development Progress & Next Steps

## 1. Current Milestone
- **Target:** Phase 2 - Dashboard UI Enrichment & Live Data Binding (Week 2)
- **Status:** 🟢 IN_PROGRESS (Dashboard UI data binding completed, transitioning to offline sync & auth integration)

## 2. Completed Tasks (What is built)
- [2026-05-15] Designed `AssetRepository`, `Account`, `Asset`, `Transaction` Kotlin data classes and `SessionManager` to secure user data isolation.
- [2026-05-15] Implemented `asset_summary_live` view mapping inside `AssetRepositoryImpl` for optimized dashboard valuation fetching.
- [2026-05-15] Created `CalculateLookthroughUseCase` to decompose complex assets (TDFs/ETFs) based on the look-through principle in `CORE_LOGIC.md`.
- [2026-05-15] Designed offline Sync repository & room entities with a Dirty Flag sync strategy and session exception handling.
- [2026-05-27] Fixed duplicate `DashboardUiState` class/interface declaration compilation error by removing the redundant file.
- [2026-05-27] Refactored `MainActivity.kt` and bound `DashboardScreen` to `DashboardViewModel` properties.
- [2026-05-27] Removed dashboard UI placeholders and replaced them with live database values from the `DashboardAsset` model.
- [2026-05-27] Completed privacy masking mode (isMasked toggle) inside `DashboardScreen` complying with `GEMINI.md`/`GUIDE_RAIL.md` guidelines.
- [2026-05-28] Fixed `BuildConfig` unresolved reference issue by removing BuildConfig condition in `SessionManager` bypass logic.
- [2026-05-28] Fixed `INTERNET` permission error by adding usage permission to `AndroidManifest.xml`.
- [2026-05-28] Resolved kotlinx.serialization crashes for `Account`, `Asset`, `Transaction` and `AssetSegment` models by applying `@Serializable`, `@SerialName` mappings and implementing custom `ZonedDateTimeSerializer`.
- [2026-05-28] Built global utility `AppLogger` (with caller information extraction and global switch) and fully integrated across UI, UseCase, Repository, and Application layers.
- [2026-06-07] Diagnosed and proposed SQL queries to debug total asset valuation anomalies caused by historical `cash` transaction quantity mismatches on `asset_id = 75`.
- [2026-06-07] Refactored `DashboardScreen.kt` to integrate detailed account views (`ProfitPerformanceCardView`, `AssetAllocationBarCardView`, `ActiveHoldingItemView`) based on Stitch design references (`account_detail_sample.kt`).
- [2026-06-08] Fixed text wrapping and layout alignment issues for long asset names in lists by ensuring a guaranteed minimum width for numeric values.
- [2026-06-08] Removed the redundant "보유 자산 목록" section from the bottom of `DashboardScreen.kt`.
- [2026-06-08] Enabled account selection dropdown and date/period filtering in `TransactionsScreen.kt`.
- [2026-06-08] Corrected price and quantity rendering logic in `TransactionsScreen.kt` for manual assets, displaying only the actual price instead of hardcoded labels.
- [2026-06-08] Enhanced currency localization format (USD/KRW) in `ActiveHoldings` lists based on the asset's specific currency.
- [2026-06-08] Dynamicized "Asset Trend" and "Performance Insight" on the dashboard by querying Supabase `daily_snapshots` based on the selected period.
- [2026-06-08] Restructured logging in `AssetRepositoryImpl` and `PortfolioRepository` to log known network/DNS errors (`HttpRequestException`, `SocketTimeoutException`) as debug logs instead of noisy error stack traces.

## 3. Active Context & Immediate Next Steps (What to do next)
- [ ] Investigate and resolve Supabase connection/DNS resolution issues (`yacnbfcdqkhcmfbflklw.supabase.co` host resolution failure) or set up local Supabase emulator.
- [ ] Verify that "Asset Trend" charts and "Performance Insight" correctly display data once Supabase connection is restored.
- [ ] Run Supabase SQL clean-up script to remove or fix anomalous transaction quantities for `asset_id = 75` to verify total balance correctness.
- [ ] Connect physical device/emulator and test Hilt injection & Supabase client queries.
- [ ] Integrate local database (Room) with `LocalTransactionEntity` and connect it to `SyncRepository` for offline caching.
- [ ] Establish Auth login flow and wire it up to `SessionManager` to retrieve and hold a valid `user_id`.
- [ ] Add Look-through visual chart representation on the dashboard UI utilizing the implemented `CalculateLookthroughUseCase`.

## 4. Known Blockers & Technical Debt
- **Blocker:** Supabase DNS resolution failure (`HttpRequestException: Unable to resolve host "yacnbfcdqkhcmfbflklw.supabase.co"`). Daily snapshots and other online queries fail or return empty states without local database fallback.
- **Blocker:** Missing Room database implementation (`TransactionDao` and `Database` class) to complete the offline sync pipeline.
- **Debt:** Currently utilizing hardcoded validation error mock for offline fallback instead of real room entity flow; needs DB wiring.
- **Bypass:** Currently bypassing Supabase Auth session inside `SessionManager` using hardcoded userId (`7f472a2b-952b-4d26-a833-de1d1b760d75`) for debugging; needs login integration to remove bypass.
