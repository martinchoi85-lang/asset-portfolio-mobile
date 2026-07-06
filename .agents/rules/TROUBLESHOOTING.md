---
trigger: always_on
---

# Troubleshooting History & Data Ingestion Rules

## 1. Deterministic Transaction Deduplication
- **The Bug**: Repeated HTS Excel or clipboard uploads caused critical balance inflation and duplication.
- **The Cure**: Enforce a rigid deterministic SHA-256 hash combination of `(date + ticker + amount + type + account_id)` applied as a database Unique Constraint and Upsert conflict target.
- **UX Pipeline**: Parsed data streams must drop into a UI Staging Area with `isDuplicateSuspected = true` if hashes collide, requiring manual verification before pushing to the ledger.

## 2. Resilient Parsing Strategies
- **Position over Header Labels**: Never map spreadsheet inputs using structural header text strings (as formats change across broker exports). Implement hardcoded index-based cell extraction (e.g., `iloc[:, 2]`).
- **Temporal Transformations**: Account for serial integers representing Excel timestamps (e.g., 46104) and normalize strictly to standard `YYYY-MM-DD` formats.
- **Row Explosion Mechanics**: When processing legacy records combining buy and sell mutations within a single log row, programmatically split the data object into two independent, sequential database ledger updates.

## 3. Data Integrity & Type Safety
- **String Format Enforcement for Tickers**: Korean stock tickers with leading zeroes (e.g., "005930") will suffer numeric truncation errors if handled as Integers. Force explicit String casing across all serialization layers.
- **Non-blocking Data Ingestion**: Prevent parsing script failure when encountering legacy components missing accurate tickers. Route exceptions to map as a generic string `'Ticker Missing'` and allow processing to complete rather than throwing fatal application crashes.
- **Null Reference Safeguards**: Neutralize unexpected platform variance errors by wrapping cell evaluation targets in clean sanitization layers like `(value or '').strip()`.

## 4. UI Race Conditions and State Emission
- **The Bug**: Jetpack Compose screens rendering empty UI states or fragmenting because the asynchronous backend use-case has not completed its execution.
- **The Cure**: Enforce a strict sequential pipeline inside `viewModelScope.launch`. Use `collectAsStateWithLifecycle` to decouple the UI from state mutations and prevent memory leaks. Never emit a `Success` state before all dependent calculations (Yield, BestAsset, TrendData) are fully resolved. Ensure any chart data structures contain proper epoch timestamps for chronological mapping to prevent layout shattering.

## 5. Declarative State Pipelines & View-First Architecture
- **The Bug**: Imperative mutation loops (like sequential `applyFilter()` calls) decouple UI state emission from continuous input streams, causing regression bugs like `trendList` not reacting to timeframe selector clicks. Additionally, embedding network data fetches (e.g., FX exchange rates) directly in Composables via `produceState` violates Clean Architecture.
- **The Cure**: Transition completely to reactive `StateFlow` structures using the `combine` operator in the ViewModel. Treat raw repository responses, selected accounts, and selected periods as individual upstream flows that merge into a single `DashboardUiState`.
- **UI Logic Purge**: The Composable layer must act solely as a rendering target. Push all format switching, time period slicing, and API network fetch coroutines (`Dispatchers.IO`) completely into the ViewModel layer. The UI must only evaluate pure boolean triggers (`isUsdDisplayPreferred`) without calculating the monetary conversions itself.

## 6. Chart Representation & MVI Rollback Issues (Recent Session)
- **The Bug**: Attempting to implement day-by-day forward-fill algorithms and linear path interpolation inside Canvas views led to broken visual pathing, empty rendering bounds, and synchronization deadlocks on smaller datasets. Additionally, UI state type mismatches (e.g. `List<Asset>` vs `List<DashboardAsset>`) and Compose block syntax errors (dangling else/if scopes inside custom Canvas drawing blocks) caused build breaks.
- **The Cure**: Demoted the Canvas components to use a discrete, simple Bar Chart representation (`drawRect` with layout bounds) directly indexing the available snapshots. Removed the over-engineered chronological forward-fill loop in favor of utilizing natively available dataset snapshots directly inside the ViewModel. Ensure compiler-enforced types in state flow models (`RawPortfolioData`) strictly align with Domain/Data repository returns.