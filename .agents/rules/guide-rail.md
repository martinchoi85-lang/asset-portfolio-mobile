---
trigger: always_on
---

# 📏 GUIDE_RAIL.md: Technical Standards & Constraints

## 1. Architecture & State Implementation
- **ViewModel Constraints:** Handle UI state only. Zero business logic. Inject top-scope ViewModels via NavHost to maintain SSOT.
- **UI State Pattern:** Use `Sealed Interfaces` (e.g., `Loading`, `Success`, `Empty`, `Error`).
- **Privacy Mode:** All monetary Composables must react to the global `isPrivacyModeEnabled` state to mask currency values.
- **Localization:** Source code, logic comments, and internal documentation must be in **English**. User-facing UI strings must be in **Korean**.
- **File Documentation Requirement:** When creating a new logic file or modifying an existing one, you MUST write a block comment at the very top of the file in **Korean**. This comment must clearly explain what logic/class the file implements, its core functionality, and any necessary context to help the user easily understand the file.
- **AI Debugging Logger (Mandatory):** When writing or modifying core logic/repositories/usecases, you MUST use `AppLogger` to print "debugging log for AI" for major return values and state changes.
  - Logger File Path: `app/src/main/java/com/choi/assetportfolio/core/util/AppLogger.kt`
  - Import Statement: `import com.choi.assetportfolio.core.util.AppLogger`

## 2. Supabase Integration & Security Policy
- **Safety Filtering:** Always validate ID/UUID/BigInt presence (`null` or `""` checks) before any `.eq()` filter to prevent SQL Error 22P02.
- **Pagination Pattern:** Use `while` loops with `range(offset, offset + limit - 1)` for data batching (Max 1,000 rows limit per request).
- **Deduplication:** Generate a deterministic `hash_key` before inserting transaction records.
- **Explicit GRANTs (Mandatory for New Tables):**
  Every new table creation script MUST include:
  ```sql
  grant select on public.your_table to anon;
  grant select, insert, update, delete on public.your_table to authenticated;
  grant select, insert, update, delete on public.your_table to service_role;
  ```

* **RLS Policy:** Always enable Row Level Security and add owner-based policies:
  ```sql
  alter table public.your_table enable row level security;
  create policy "Users can manage their own data" on public.your_table for all to authenticated using (auth.uid() = user_id) with check (auth.uid() = user_id);
  ```

* **Kotlin Exception Handling:** Catch `RestException` with `errorCode == "42501"` for permission fallbacks.

## 3. Test-Driven Development (TDD) Policy

* **Failing Test First:** Create a failing test case for P/L and Yield logic before implementing changes.
* **Mocking:** Mock database/Supabase responses using MockK to isolate unit tests.