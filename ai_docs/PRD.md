# Product Requirements Document (PRD)

## 1. Target Product
- **Name**: Personal Asset Portfolio Management Native Android App
- **Core Value**: Unified monitoring of automated (Stocks/ETFs) and manual assets (Savings, TDF, Funds, Cash) with strict local-first resilience.

## 2. Epics & Core Features (4-Tab Architecture)
1. **Dashboard (Screen 1)**: View-centric asset summary. Horizontal `ScrollableTabRow` account filter (All, Bank A, Sec B, +Add Account). Real-time portfolio valuation filtering.
2. **Analysis (Screen 2)**: X-ray asset decomposition. Triggers `GetLookthroughAllocationUseCase` to explode complex products (ETF/TDF) into underlying asset classes. Rebalancing guide UI.
3. **Transactions (Screen 3)**: Ledger & Deduplication Hub. Dual-layer layout (Staging UI for duplicate/unapproved imports vs. Hardened Historical View).
4. **Management Hub (Screen 4)**: Manual Asset Controller. Event-driven principal modifications input forms.

## 3. Product Constraints & Guardrails
- **UX Priority**: View-First, Manage-Second. Heavy display optimization; mutation screens strictly isolated.
- **Data Security**: Multi-portfolio ready. Every query must filter down to single tenant execution via authenticated `user_id`.