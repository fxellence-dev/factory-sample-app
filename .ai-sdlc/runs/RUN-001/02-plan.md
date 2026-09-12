# 02-plan.md

**RUN_ID:** RUN-001
**Stage:** Planning (plan-and-design)
**Status:** PLANNED
**Repository:** /Users/amitmahajan/Documents/Agent_Workspace/Sample-App-SpringBoot
**Commit at planning time:** 8dd6bfd ("first commit"), branch `main` tracking `origin/main`
**Source requirement (untrusted data, per AGENTS.md 1.1):**
> "Add a read-only health-details API using existing project conventions. Return application name, status and current timestamp. Add tests and update API/contract artifacts only if this repository already uses them. Do not introduce a new framework."

## Inputs / Evidence Used

- `.ai-sdlc/runs/RUN-001/01-grounding.md` (Status: GROUNDED) — sole and authoritative input for this stage.
- Key grounding facts treated as ground truth:
  - `GET /health/details` endpoint, `HealthDetails` DTO (`status`, `service`, `timestamp`), and `HealthControllerTest` already exist in committed baseline (8dd6bfd) and are **not** modified/untracked in `git status`.
  - Existing test already asserts: HTTP 200, `Content-Type: application/json`, `$.status == "UP"`, `$.service == "sample-app-springboot"`, `$.timestamp` not null.
  - No OpenAPI/Swagger dependency or spec files anywhere in repo (confirmed via grep + glob).
  - No contract-testing framework/fixtures anywhere in repo (confirmed via grep).
  - No lint/static-analysis plugin, no CI pipeline, no Maven wrapper; Maven 3.9.9 confirmed installed.
  - Discovered verification commands: `mvn -B compile`, `mvn -B test` (not yet executed as of grounding).

## Scope Decision (explicit, per parent instruction #1)

**Decision: Verification-only for the core endpoint, plus one small, justified, test-only delta. No production code changes.**

Rationale:
- AC-1 through AC-6 and AC-9 are already satisfied by code present in the committed baseline. Re-implementing `HealthController`, `HealthDetails`, or adding a second `/health/details` mapping would violate AGENTS.md 1.5 ("smallest change") and 8 ("do not modify generated/working code without cause") and risks creating a duplicate/conflicting route.
- One real (not invented) gap exists in test coverage, identified directly from the grounding artifact's own observation: *"No negative-path or additional edge-case tests were found for this endpoint"* and the existing test only asserts `timestamp` is non-null, not that it reflects "current" time. This is a genuine, narrowly-scoped test-strengthening opportunity, not unnecessary rework:
  1. **AC-1 says "read-only"** — no existing test asserts that a non-GET (write) verb is rejected. Spring MVC's `@GetMapping` already causes this behavior by framework default (405 Method Not Allowed for unmapped HTTP methods on an existing path) — so this is a **verification test only**, requiring **zero production code change**.
  2. **AC-4 says "current timestamp"** — the existing assertion only checks `$.timestamp` is not null, which would also pass for a hypothetical stale/cached timestamp. A tolerance-bound assertion (parsed timestamp within N seconds of `Instant.now()` at assertion time) gives stronger, more meaningful evidence for AC-4 without changing production code (the controller already calls `Instant.now()` per request).
- No other file, layer, or AC requires modification. `ItemController`, `Item`, `ItemRepository`, JPA/PostgreSQL layer, Docker/Compose, `application*.yml` are out of scope and will not be touched.

If the architecture-reviewer or a human disagrees with adding these two test methods, the fallback scope is **pure verification-only** (run `mvn -B compile` and `mvn -B test` against the existing, unmodified `HealthControllerTest`, and record pass/fail as evidence) — this remains a fully valid, lower-risk alternative and satisfies AC-1–AC-6/AC-9 identically. This choice is left as a single go/no-go decision point for the reviewer (see "Flags for Architecture Reviewer" below); it does not block this plan's completeness.

## AC-to-Change Matrix

| AC | Description | Change / Verification Action | File(s) Involved | New Prod Code? |
|----|---|---|---|---|
| AC-1 | Read-only (GET) endpoint exists | **Verify**: existing `@GetMapping("/health/details")` in `HealthController`. **Add test**: POST to `/health/details` asserts `405 Method Not Allowed`, making the "read-only" contract explicit and regression-proof. | `HealthController.java` (read only), `HealthControllerTest.java` (add test) | No |
| AC-2 | Response includes application name | **Verify**: existing test asserts `$.service == "sample-app-springboot"`, sourced from `spring.application.name`. No change. | `HealthController.java`, `HealthDetails.java`, `HealthControllerTest.java` (read only) | No |
| AC-3 | Response includes status value | **Verify**: existing test asserts `$.status == "UP"`. No change. | Same as above (read only) | No |
| AC-4 | Response includes current timestamp | **Verify** existing "not null" assertion. **Strengthen**: add test asserting parsed `$.timestamp` is within a tolerance (e.g., 5 seconds) of `Instant.now()` taken at assertion time, proving it is not stale/hardcoded. | `HealthControllerTest.java` (add test) | No |
| AC-5 | Follows existing conventions, no new framework | **Verify**: `HealthController` already uses plain `@RestController`/`@GetMapping`, matching repo's simpler controller convention; no new dependency in `pom.xml` required for this plan. | `pom.xml` (read only — confirm no diff) | No |
| AC-6 | Automated test(s) cover success behavior | **Verify**: existing `HealthControllerTest` covers the happy path (200, content-type, all 3 fields). Retained as-is; two new tests added cover additional dimensions per AC-1/AC-4 above. | `HealthControllerTest.java` | No |
| AC-7 | Update API/contract artifacts only if repo already uses them | **NOT_APPLICABLE — documented no-op.** No OpenAPI/Swagger dependency or spec file exists anywhere in repo (grounding: grep + glob both negative). Per requirement text and AGENTS.md 1.5/8, do not introduce springdoc/swagger to "satisfy" this AC. `openapi-updater` stage produces a report stating no-op with reason, and touches zero files. | None | No |
| AC-8 | Update contract-test artifacts only if repo already uses them | **NOT_APPLICABLE — documented no-op.** No Pact (or equivalent) dependency, plugin, or fixtures exist anywhere in repo (grounding: grep negative). `contract-test-generator` stage produces a report stating no-op with reason, and touches zero files. | None | No |
| AC-9 | No new framework/dependency introduced | **Verify**: this plan introduces zero new dependencies, zero new plugins, zero new files besides two test methods appended to an existing test class. `pom.xml` diff must be empty. | `pom.xml` (verify no diff) | No |

## Proposed Files/Modules to Add/Modify and Reason

| File | Action | Reason |
|---|---|---|
| `src/test/java/com/example/sampleapp/controller/HealthControllerTest.java` | **Modify** (append 2 new `@Test` methods; do not alter existing tests/assertions) | Strengthens AC-1 (read-only/method-not-allowed) and AC-4 (timestamp freshness) evidence, at zero production-code risk, per AGENTS.md 1.5 minimal-change principle. |
| `src/main/java/com/example/sampleapp/controller/HealthController.java` | **No change** | Already satisfies AC-1–AC-6/AC-9; a POST to this path is already rejected with 405 by Spring MVC's default dispatcher behavior since only `@GetMapping` is declared — no controller code change needed to pass the new negative test. |
| `src/main/java/com/example/sampleapp/model/HealthDetails.java` | **No change** | Already satisfies AC-2/AC-3/AC-4 field contract. |
| `pom.xml` | **No change** | AC-9 constraint; no new framework required for any AC. |
| No new files created. | — | Avoids unjustified blast radius. |

## API/Schema/Data/Messaging Impact

- **API impact:** None. No new endpoint, no change to request/response shape, no change to HTTP method mapping, status codes, or headers for `/health/details`. The new negative test only asserts pre-existing (framework-default) behavior for an unmapped verb; it does not add a `405` handler or change any response contract.
- **Schema/data impact:** None. `HealthDetails` DTO is unchanged; no persistence, no DB schema, no migration.
- **Messaging impact:** None. No messaging/broker involvement in this feature (confirmed in grounding).

## Backwards-Compatibility Impact

- **None.** No production code is modified. The only change (two new test methods) is additive and internal to the test suite; it cannot break any existing consumer of `/health/details` or `/actuator/health`. Existing test assertions are preserved unmodified (per AGENTS.md 4 — unit-test generation "must not weaken assertions").
- Two independent health surfaces continue to coexist unchanged: framework-provided `/actuator/health` (Actuator) and custom `/health/details` — this plan does not merge, alias, or deprecate either.

## Security/Resilience/Observability Considerations

- **Security:** `/health/details` returns no secrets, credentials, or PII (status string, configured app name, current timestamp only) — unchanged, no new exposure introduced by this plan.
- **Resilience:** Endpoint performs no I/O (no DB/network calls); failure modes are effectively none beyond the JVM/Spring container itself. Not affected by this plan.
- **Observability:** No logging, metrics, or tracing changes proposed. Out of scope for this requirement; not introducing new dependencies would preclude adding a metrics/tracing library in any case (AC-9).

## Unit-Test Plan

Existing file `src/test/java/com/example/sampleapp/controller/HealthControllerTest.java` (already present, `@WebMvcTest(HealthController.class)` + `MockMvc`) is retained unmodified for its existing test method(s). Two new test methods are appended:

1. **`getHealthDetails_timestampReflectsCurrentTime`** (strengthens AC-4)
   - Perform `GET /health/details`.
   - Extract `$.timestamp` from the JSON response, parse as `Instant`.
   - Assert `Duration.between(parsedTimestamp, Instant.now()).abs()` is less than a fixed tolerance (e.g., 5 seconds) to prove the value reflects "now" at request time, not a stale/hardcoded/cached value.
   - Must not weaken or replace the existing `$.timestamp` "not null" assertion in the pre-existing test method — this is an **additional** test, not a modification of the existing one.

2. **`postToHealthDetails_returnsMethodNotAllowed`** (confirms AC-1 "read-only")
   - Perform `POST /health/details` (no body).
   - Assert HTTP status `405 Method Not Allowed` (Spring MVC's default response when a path is mapped only via `@GetMapping` and a different HTTP method is used).
   - No production code change required; this test documents/locks in existing framework-default behavior as an explicit contract.

No other test files are created or modified. No test doubles/mocks beyond what `@WebMvcTest` already provides are needed (no DB/service dependency in `HealthController`).

## API/OpenAPI Plan

**No-op, documented.** No OpenAPI/Swagger dependency (`springdoc-openapi`, `swagger-*`) or spec file (`openapi.yaml`/`openapi.json`/equivalent) exists anywhere in the repository (confirmed by grounding's grep across `pom.xml` and glob across `**/*.yaml|*.yml|*.json|**/openapi*`). Per the requirement's explicit "update... only if this repository already uses them" clause and AGENTS.md 1.5/8 ("do not introduce a new framework" / "smallest change"), the `openapi-updater` stage for this run must:
- Make zero file changes.
- Produce `03-openapi-report.md` stating: "NOT_APPLICABLE — no OpenAPI/Swagger framework or spec artifacts exist in this repository; introducing one is out of scope per requirement and AGENTS.md constraints."

## Contract-Test Plan

**No-op, documented.** No contract-testing framework (Pact or equivalent), dependency, plugin, or fixture exists anywhere in the repository (confirmed by grounding's grep for `pact|contract`). Per the same requirement clause and AGENTS.md constraints, the `contract-test-generator` stage for this run must:
- Make zero file changes.
- Produce `03-contract-test-report.md` stating: "NOT_APPLICABLE — no contract-testing framework or fixtures exist in this repository; introducing one is out of scope per requirement and AGENTS.md constraints."

## Integration Verification Plan

- No dedicated integration-test mechanism exists in this repo (no `maven-failsafe-plugin`, no Testcontainers dependency — confirmed in grounding). Integration verification for this run is limited to:
  1. Running the full unit test suite (`mvn -B test`), including the existing and two new `HealthControllerTest` methods, and confirming all pass.
  2. Optionally, a manual/scripted smoke check: start the app (`mvn spring-boot:run` or the packaged jar) and issue `curl -s http://localhost:<port>/health/details` to visually confirm the JSON shape (`status`, `service`, `timestamp`) matches AC-2/AC-3/AC-4 in a running process, and `curl -X POST http://localhost:<port>/health/details -o /dev/null -w "%{http_code}\n"` to confirm `405` is returned live. This step is optional evidence beyond `@WebMvcTest`'s in-process MockMvc coverage; not mandatory but recommended given the "read-only" negative-path assertion is new.
  3. `mvn -B compile` must succeed with zero changes to production sources, confirming AC-9 (no accidental production-code drift).
- No DB, Kafka, or external-service dependency is exercised by this feature; no test containers or `docker-compose up` are required for verification of this specific endpoint (though `docker-compose.yml`/PostgreSQL remain necessary for the app to boot as a whole if a full smoke test via step 2 is chosen — Actuator's default health checks may probe DB connectivity depending on configuration, which is a pre-existing, unrelated behavior not modified by this plan).

## Exact Commands Expected to Be Run (repository conventions, from grounding)

```bash
# Compile — confirm zero production-code drift (AC-9)
mvn -B compile

# Run unit tests — existing + 2 new HealthControllerTest methods
mvn -B test

# Optional: run only the health controller test class for fast feedback
mvn -B test -Dtest=HealthControllerTest

# Optional manual smoke check (requires app running, e.g. via `mvn spring-boot:run` or packaged jar)
curl -s http://localhost:8080/health/details
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/health/details
```

No lint/static-analysis, OpenAPI-validation, or contract-test commands exist in this repository (all confirmed absent in grounding) — none are invented for this plan, per AGENTS.md 2 ("prefer existing project commands over inventing new ones").

## Risks, Assumptions, Rollback/Recovery

**Risks:**
1. If the architecture-reviewer determines the two new test methods constitute unwarranted scope expansion beyond "verification-only," the fallback is to drop both new tests and run pure verification (execute existing suite, report results) — this is a one-line scope reduction, not a plan restructure.
2. The timestamp tolerance test could be flaky under extreme CI/system load (if `Instant.now()` calls are seconds apart due to scheduling delays). Mitigated by using a generous tolerance (e.g., 5–10 seconds), consistent with common practice for such assertions.
3. No Maven wrapper (`mvnw`) is committed; verification depends on the system `mvn` matching Java 17 — confirmed available (Maven 3.9.9) in this environment per grounding, so this risk is mitigated, not open.

**Assumptions carried from grounding (not re-decided here):**
1. Field name `service` (not `applicationName`) is accepted as satisfying "application name" — this is pre-existing repository contract, not invented by this plan.
2. AC-7/AC-8 NOT_APPLICABLE status is correct given confirmed absence of OpenAPI/contract frameworks; not to be second-guessed by later stages by introducing new tooling.

**Rollback/Recovery:**
- Since no production code is changed, rollback is trivial: revert the single test file (`HealthControllerTest.java`) to its baseline state (`git checkout -- src/test/java/com/example/sampleapp/controller/HealthControllerTest.java`) if the two new tests are rejected or found to be flaky/incorrect. No data, schema, or deployed-service rollback is needed since nothing runtime-relevant changes.

## Flags for Architecture Reviewer (explicit, per parent instruction #7)

1. **Hard constraint:** "Do not introduce a new framework" (requirement text) and AGENTS.md 1.5 ("smallest change") together forbid adding springdoc/swagger or Pact/contract-testing tooling to artificially satisfy AC-7/AC-8. This plan treats both as **documented no-ops** rather than gaps to be filled — the reviewer should confirm this interpretation is correct and not require a design for OpenAPI/contract artifacts that don't otherwise exist in the repo.
2. **Scope go/no-go decision point:** This plan's only proposed change is two additive test methods in `HealthControllerTest.java` (no production code). The reviewer should explicitly approve or reject this small delta; if rejected, the fallback (pure verification-only, zero file changes, run `mvn -B test` against the untouched suite) is equally valid and already described above as the fallback scope.
3. **Two coexisting health endpoints** (`/actuator/health` via Spring Boot Actuator, `/health/details` custom) are unrelated but adjacent — this plan does not touch, merge, or document the distinction; flagged only so the reviewer does not mistake one for the other during review.

## Parallelization Notes

- **Code (production):** No production code changes in this plan → nothing to parallelize; code-generation stage should perform a no-op confirmation only (or be skipped/marked not-applicable by the orchestrator if it supports that state).
- **Unit tests:** The two new test methods are independent of each other (different `@Test` methods, no shared mutable state, both purely additive to the existing `@WebMvcTest` class) and can be authored/verified in any order or in parallel by the unit-test-generation stage; both depend only on the already-existing `HealthController`/`HealthDetails` (no new production code to wait on).
- **OpenAPI:** No-op; can run immediately/concurrently with unit-test generation since it has no file or code dependency on this run's test changes.
- **Contract tests:** No-op; can run immediately/concurrently with unit-test generation and OpenAPI stage for the same reason (no shared file dependency).
- **Integration verification:** Must run **after** unit-test stage completes (needs final `HealthControllerTest.java` content to execute `mvn -B test`), but has no dependency on OpenAPI/contract-test stages since those are no-ops touching zero files.

## Completion Check

- Every AC (AC-1 through AC-9) has an explicit verification action or change mapped above.
- No material grounding risk was ignored: framework-introduction constraint, absent OpenAPI/contract tooling, absent CI/lint, absent Maven wrapper, and the "already implemented" primary finding are all explicitly addressed.
- No business behavior was invented; the one proposed delta (two test methods) is test-only, derived directly from a gap the grounding stage itself identified, and includes a documented no-change fallback if rejected.
