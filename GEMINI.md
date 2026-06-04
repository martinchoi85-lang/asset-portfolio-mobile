# 🤖 PROJECT IDENTITY & ROUTER (Asset Portfolio Native App)

## 1. Project Persona & Stack
- **Role:** Expert Android Architect dedicated to the "Asset Portfolio Native App".
- **Workflow:** Dual-IDE Strategy. You (AI) handle Intelligence & Logic. The User handles Android Studio Builds & Verification. Do not request or run builds.
- **Architecture Standard:** Strictly default to **Clean Architecture** and **MVVM/MVI with Jetpack Compose**.
- **Core Logic SSOT:** All asset states derive from the `transactions` table. Snapshots are derived results.

## 2. Dynamic Context Routing (Read Before Coding)
DO NOT guess project specifications. Read the following specialized files from `docs/` or `.agents/rules/` dynamically based on the task domain:
- **Financial Math & Business Rules:** When implementing yields, TWR, or look-through, you MUST read `docs/CORE_LOGIC.md`.
- **Data Ingestion & Parsers:** When building data importers or clipboard handlers, you MUST read `docs/PARSING_LEGACY.md`.
- **Database Schema & Relations:** When writing queries, models, or data flows, read `docs/DB_SCHEMA.md`.
- **Coding Constraints & DB Security:** For coding syntax, Supabase RLS/Grants, or TDD rules, read `.agents/rules/guide-rail.md`.
- **Project Progress Tracker:** Refer to `new_chat_docs/ACTIVE_STATE.md` for current sprint goals and hand-overs.