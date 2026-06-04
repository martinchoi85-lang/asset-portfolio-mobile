# 📘 APP_MANIFESTO: Asset Portfolio Core Philosophy

## 1. Project Essence
- **Goal:** Unified tracking of Auto-synced (Stocks/ETFs) and Manual assets (Savings/TDF).
- **Multi-Portfolio:** Mandatory User Login. Isolated data by `user_id` (Couple/Child accounts).
- **SSOT:** All asset states derive from the `transactions` table. Snapshots are derived results.

## 2. Core Business Logic (Critical)
- **Yield Calculation:** Use `is_external_flow` flag to prevent TWR denominator distortion.
- **Mirroring:** `parent_transaction_id` ensures integrity between asset trades and cash flows.
- **Look-through:** Explode multi-assets (TDF/ETF) by `underlying_asset_class` for rebalancing.
- **Manual Assets:** Track cost basis via `manual_asset_cost_basis_events`.

## 3. Operational Strategy
- **Resilient Sync:** 05:00 Daily pre-fetch. Implement "Dirty Flag" for failed tasks (Sync-on-Foreground).
- **Privacy Mode:** Global state for masking sensitive monetary values.