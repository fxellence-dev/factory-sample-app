# Factory AI-SDLC Starter

A technology-agnostic staged AI-SDLC starter for Factory Droid CLI/Desktop.

## Included

- Root `AGENTS.md`
- 11 project custom Droids in `.factory/droids/`
- `FIRST_RUN.md` manual orchestration prompt
- `.ai-sdlc/runs/` artifact root

## Model allocation

| Droid | Model | Purpose |
|---|---|---|
| requirement-grounder | claude-sonnet-5 | strong repository grounding |
| planner | claude-sonnet-5 | implementation planning |
| architecture-reviewer | gpt-5.6-sol | independent plan challenge |
| code-generator | claude-sonnet-5 | implementation |
| unit-test-generator | gpt-5.3-codex | tests from separate family |
| openapi-updater | gemini-3.7-flash | lower-cost structured spec work |
| contract-test-generator | gpt-5.3-codex | contract test implementation |
| integrator | claude-sonnet-5 | execution/verification only |
| critic | gpt-5.6-sol | mandatory different family from generator |
| judge | claude-opus-5 | high-quality final change decision |
| rubric-evaluator | gemini-3.1-pro-preview | independent process evaluation |

Model IDs reflect Factory's current public model catalogue at bundle creation time. If your Factory organization blocks a pinned model, Factory may fall back to the parent model; verify resolved models with `/droids` before relying on critic independence.

## Install

Copy the contents into your sample repository root, then run:

```bash
droid
```

Inside Droid:

```text
/droids
```

Verify all project Droids load without validation errors. Then follow `FIRST_RUN.md`.

## Why no MCP servers yet?

Every Droid sets `mcpServers: []` for the first sample run. This removes accidental Jira/GitHub/other external side effects and makes the experiment reproducible from local repository evidence. Add approved MCP access later per Droid.

## Why this is technology-agnostic

The Droids discover build/test/spec/contract mechanisms from repository evidence. The rubric evaluates stage outcomes, autonomy, control and evidence—not Maven, .NET, CMake or any specific toolchain.
