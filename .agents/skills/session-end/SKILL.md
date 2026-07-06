---
name: session-end
description: Wrap-up protocol to update active state, regenerate structure, and prepare handover package
triggers:
  - command: "/end"
  - intent: "Terminate session or wrap up development"
scope: workspace
requirements:
  - python3
---

# Session Wrap-up Protocol

You are tasked with finalizing the current development session. Execute the following automation and documentation steps without conversational filler.

## Instructions
1. **Automated Structure Update**: Execute the project layout extraction script via the terminal subsystem:
   `python3 .agents/skills/session-end/scripts/generate_structure.py`
2. **Session Assessment**: Evaluate all programmatic refactors, model creations, and file mutations executed over the course of the current chat lifetime.
3. **State Preservation**: Overwrite `ai_docs/active_state.md` to cleanly record current completions, blocker resolutions, and pending execution roadmaps for the upcoming session.
4. **Git Structuring**: Draft a clear commit statement matching Conventional Commits standards (e.g., `feat(ui): ...`, `fix(domain): ...`).

## Output Constraints & Formatting
Provide the final output package instantly without introductory dialogue. Deliver the block using the precise structure below for frictionless clipboard handovers:

### 📋 [Handover Packet for Supervisor]
**1. Summary of Changes:**
- [Provide brief, bulleted summaries of operations performed]

**2. File Update Confirmation:**
- [X] `ai_docs/STRUCTURE.md` (Regenerated via automation)
- [X] `ai_docs/active_state.md` (State persisted)

**3. Conventional Commit Message:**
```text
<Insert the generated commit message here>

```