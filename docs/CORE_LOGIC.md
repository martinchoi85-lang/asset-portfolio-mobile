# 🧠 CORE_LOGIC.md: Financial Logic Specifications

## 1. Cash Mirroring & Yield Distortion
- **Issue:** Auto-mirrored cash flows for buy/sell were treated as external capital.
- **Solution:** Use `is_external_flow` flag in `transactions`.
    - `True`: External deposit/withdrawal (Impacts TWR denominator).
    - `False`: Internal asset swap/mirroring (Ignored in TWR denominator).
- **Integrity:** `parent_id` (Self-referencing FK) connects mirror trades to primary trades.

## 2. Look-through & Rebalancing
- **Logic:** Explode vehicles (ETFs/Funds) into components based on `underlying_asset_class`.
- **Optimization:** Single query to `asset_segments` using `in_()` filter.

## 3. Manual Asset Policy
- No `quantity` concept (default to 1).
- Track performance via `manual_asset_cost_basis_events`.