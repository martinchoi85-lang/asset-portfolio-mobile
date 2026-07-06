---
trigger: always_on
---

# Development Rules & Technical Constraints

## 1. Architecture & State Management
- **Clean Architecture Enforcement**: Strictly isolate layers (`domain` handles pure business logic with no Android/Supabase imports, `data` implements repositories, `presentation` manages UI).
- **ViewModel Constraints**: Handle UI state only via MVI/MVVM pattern. Zero business logic allowed inside ViewModels. Inject top-scope ViewModels via `NavHost` to maintain SSOT.
- **UI State Pattern**: Every screen must expose a single State stream using `Sealed Interfaces` (e.g., `Loading`, `Success`, `Empty`, `Error`).
- **Privacy Mode**: All currency and valuation Composables must dynamically observe the global `isPrivacyModeEnabled` state and mask values when `true`.
- **UI Language**: Use **Korean language** for the newly written UI.
- **File Header Requirement**: Every new or modified file MUST include a top-block comment in **Korean** explaining its exact responsibility and functional boundaries.

## 2. Core Financial Business Logic Guardrails
- **TWR Yield Protection**: Internal asset swaps (e.g., stock liquidation mirroring into cash) must NOT alter the Time-Weighted Return denominator. 
  - Leverage `is_external_flow` (Boolean) in `transactions` table. 
  - `True` = Real capital injection/withdrawal (triggers TWR denominator recalculation).
  - `False` = Internal swap/mirroring (ignored in TWR denominator). Link via self-referencing foreign key `parent_transaction_id`.
- **Look-through Allocation**: Vehicles like ETFs/TDFs must be exploded into actual underlying asset classes. Optimize via Supabase `in_()` filter against `asset_segments` to retrieve weights in a single batch query. GroupBy/Aggregate instantly; do not execute inner loops.
- **Manual Asset Ledger**: Manual assets (Savings, TDF) have no unit price or quantity concept (default quantity = 1). Track cost basis fluctuations strictly via `manual_asset_cost_basis_events` (using delta values and an explicit `reason` like maturity or top-up). Maintain active status cache in `manual_asset_cost_basis_current`.

## 3. Supabase Integration & Database Security
- **Explicit Database GRANTs**: Every new table migration script MUST include:
  ```sql
  grant select on public.your_table to anon;
  grant select, insert, update, delete on public.your_table to authenticated;
  grant select, insert, update, delete on public.your_table to service_role;

```

* **Row Level Security (RLS)**: Enforce user-scoped access control on all backend mutations:
```sql
alter table public.your_table enable row level security;
create policy "Users can manage their own data" on public.your_table for all to authenticated using (auth.uid() = user_id) with check (auth.uid() = user_id);

```


* **Network Safety**: Validate presence of all UUIDs/IDs before appending `.eq()` filters to prevent SQL 22P02 crashes. Batch process data fetches using a `while` loop pagination pattern bounded by `range(offset, offset + limit - 1)` (Max 1,000 rows per request). Catch `RestException` matching `errorCode == "42501"` for local fallback orchestration.

## 4. Test-Driven Development (TDD) Policy

* **Failing Test First**: Write failing edge-cases for P/L and Yield logic mutations before writing implementation code.
* **Mocking boundary**: Use MockK to isolate unit tests by mocking database operations and network interactions.