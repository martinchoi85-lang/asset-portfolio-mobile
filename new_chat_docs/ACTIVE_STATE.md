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
- [2026-05-28] Registered mandatory logging constraints inside `guid-rail.md` policy.

## 3. Active Context & Immediate Next Steps (What to do next)
- [ ] Verify Dashboard data binding works properly now that RLS disablement (Method A) or anon bypass policy (Method B) is applied on Supabase.
- [ ] Connect physical device/emulator and test Hilt injection & Supabase client queries.
- [ ] Integrate local database (Room) with `LocalTransactionEntity` and connect it to `SyncRepository` for offline caching.
- [ ] Establish Auth login flow and wire it up to `SessionManager` to retrieve and hold a valid `user_id`.
- [ ] Add Look-through visual chart representation on the dashboard UI utilizing the implemented `CalculateLookthroughUseCase`.

## 4. Known Blockers & Technical Debt
- **Blocker:** Missing Room database implementation (`TransactionDao` and `Database` class) to complete the offline sync pipeline.
- **Debt:** Currently utilizing hardcoded validation error mock for offline fallback instead of real room entity flow; needs DB wiring.
- **Bypass:** Currently bypassing Supabase Auth session inside `SessionManager` using hardcoded userId (`7f472a2b-952b-4d26-a833-de1d1b760d75`) for debugging; needs login integration to remove bypass.
