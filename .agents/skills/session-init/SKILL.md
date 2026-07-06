---
name: session-init
description: Bootstrap protocol to synchronize project context using optimized ai_docs
triggers:
  - command: "/init"
  - intent: "Initialize or setup session context"
scope: workspace
---

# Session Bootstrap Protocol

You are tasked with initializing the development session context. Follow these procedural steps immediately when triggered:

## Instructions
1. **File Detection**: Scan the `ai_docs/` directory to verify the existence of core architectural blueprints: `PRD.md`, `STRUCTURE.md`, `DB_SCHEMA.md`, and `active_state.md`.
2. **Execution Branch**:
   - **IF CHANNELS EXIST**: Systematically digest all discovered markdown guidelines inside `ai_docs/` to absorb the exact product design goals, technical constraints, accounting mathematical algorithms, and ongoing development states.
   - **IF CHANNELS ARE MISSING**: Skip context sync and analyze the active Android project workspace topology directly (inspecting build variants, Gradle manifests, and dependency graphs). Ask the user for their targeted entry point.

## Constraints
- Supress verbose diagnostic outputs unless facing fatal file parsing conditions.
- Following successful initialization, print a highly concise 3-line status overview of `active_state.md` and await structural code commands.