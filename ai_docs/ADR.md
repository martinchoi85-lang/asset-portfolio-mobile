# Architecture Decision Records (ADR)

## ADR 001: Local-First with Cloud-Sync Engine
- **Context**: App requires instantaneous data fetching and offline availability despite financial network fluctuations.
- **Decision**: Room DB acts as the Single Source of Truth (SSOT) for Jetpack Compose UI layers. Supabase PostgreSQL acts as the remote backing ledger, synchronized bi-directionally via a isolated Sync Engine.

## ADR 002: Transaction-Centric State Derivation
- **Context**: Pre-calculated snapshots cause data drifts and historical alignment failures.
- **Decision**: All asset valuations, net cash flows, and performance tracking are derived dynamically from the transactional ledger. Snapshots are treated strictly as ephemeral query performance caches.

## ADR 003: Mitigation of Time-Weighted Return (TWR) Distortion
- **Context**: Internal asset swaps (e.g., selling stock into cash mirror) inflate the TWR denominator, corrupting return tracking.
- **Decision**: Introduce `is_external_flow` (Boolean) in transactions table. Only true capital injections/withdrawals trigger TWR updates. Swaps maintain `is_external_flow = false` and link via `parent_transaction_id`.

## ADR 004: Event-Sourcing for Manual Assets
- **Context**: Savings/TDF lack conventional unit pricing, breaking Simple Moving Average (SMA) logic.
- **Decision**: Track via `manual_asset_cost_basis_events` (delta changes with explicit reasons like maturity, top-up) and state-cache the latest value in `manual_asset_cost_basis_current`.

## ADR 005: Deterministic Transaction Deduplication
- **Context**: Raw HTS/Excel uploads risk duplication.
- **Decision**: Apply deterministic SHA-256 hash of `(date + ticker + amount + type + account_id)` as a DB Unique Constraint. Suspected rows are routed to UI Staging Area (`isDuplicateSuspected = true`).