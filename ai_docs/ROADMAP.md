# Project Development Roadmap

## Phase 1: Infrastructure & Core Engine (Week 1) - [COMPLETED]
- Supabase Kotlin SDK integration, Ktor transport client configuration.
- Room DB schema normalization & SQLite multi-tenant structure validation.
- Deployment of `com.choi.assetportfolio.core.util.AppLogger`. Build failure rate 0%.

## Phase 2: Core UX Layer & Data Flow Wiring (Weeks 2-4) - [IN PROGRESS]
- Jetpack Compose Navigation architecture with 4 core destination graphs.
- Implementation of `CalculatePortfolioYieldUseCase` and `GetLookthroughAllocationUseCase`.
- **Current Milestone**: Resolving Session Bypass UI Lock (State Lock) during real device execution.

## Phase 3: Synchronizer & Deduplication Gate (Weeks 5-6) - [PENDING]
- Staging Area UI execution for transaction batch processing.
- Background worker for Room-Supabase bidirectional sync synchronization.

## Phase 4: Hardening & Multi-Tenant Isolation (Weeks 7-8) - [PENDING]
- Production-grade Supabase RLS policies enforcement (GRANT anon/authenticated/service_role).
- Cryptographic verification for offline storage and localized biometrics security.