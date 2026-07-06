# DESIGN.md - Unified Asset Portfolio App

## 1. Core Principles
* **Priority 1 (View-First):** Optimized for high-frequency asset monitoring and performance tracking.
* **Priority 2 (Efficient Management):** Simplified UI for adding/editing transactions and manual assets.
* **Unique Value Prop:** Seamlessly integrates **Auto-Synced Assets** (Stocks, ETFs via API) and **Manual Assets** (Savings, TDF, Funds) into a unified visualization.

## 2. Navigation Structure (Jetpack Compose Hierarchy)
### A. Top Navigation (TabRow)
* **Type:** Horizontal Scrollable TabRow.
* **Items:** [Total Portfolio] → [Individual Account A] → [Individual Account B] → [+] (Add Account Button).

### B. Main Content (Dashboard)
* **Total Portfolio View:**
    * Total Asset Value Trend (Line Chart).
    * Total Yield (%) & P/L Status.
    * Asset Allocation Chart (Pie/Donut Chart by category).
* **Individual Account View:**
    * Time-period Performance (Daily/Weekly/Monthly Yield).
    * Account-specific Asset Allocation.
    * **AssetList:** LazyColumn displaying individual holdings (Ticker, Price, Change, Value).

### C. Bottom Navigation (NavigationBar)
1.  **Dashboard:** The primary overview screen.
2.  **Analysis:** Detailed portfolio analytics (Risk, Correlation, etc.).
3.  **Transactions:** History of buying/selling and manual entries.
4.  **Settings:** User profile, data sync, and app preferences.

## 3. UI/UX Details
* **User Profile:** Clicking the profile picture icon in the top-left corner takes you to the login screen, and logging in displays details about the user's assets.
* **Privacy Mode:** A 'Eye' icon toggle in the TopAppBar to mask sensitive monetary values (e.g., "$10,230" → "****") for screenshots/sharing.
* **Color System:**
    * Background: #FFFFFF (Clean White).
    * Bullish (Gain): #af101a (Deep Red).
    * Bearish (Loss): #005faf (Deep Blue).
* **Localization:** Default language is **Korean (KR)**.
* **Typography:** Clear, sans-serif fonts optimized for financial data readability.

## 4. Technical Guardrails for UI
* **State-Driven UI:** UI must react to `AssetState` (Syncing, Success, Error).
* **Hybrid Data Source:** Differentiate UI indicators between auto-updated market data and manually entered cost basis.