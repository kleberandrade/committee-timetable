# Verification record — 2026-11-17

The latest Clean Architecture follow-up passed **38 tests** with PostgreSQL, four compiled dependency rules and formatting enforcement. Earlier counts below are retained as historical evidence; see the follow-up section for current results.

## Initial modernization: executed locally

- Java 21 (Homebrew OpenJDK), Maven 3.9.11 via the generated Wrapper, macOS.
- `./mvnw clean verify` and subsequent `verify` after desktop corrections: **20 tests, zero failures/errors/skips** with `COMMITTEE_DB_TEST=true` and the isolated PostgreSQL running. Seven test classes cover scheduling, file boundaries/CSV, CLI behavior, presentation text, Swing table models and persistence.
- PostgreSQL 17.7 in Compose project `committee-timetable`, loopback port 55439, own named volume. Hibernate schema validation, ProjectType CRUD, failed foreign-key deletion rollback/recovery and relationship/time round-trips all executed. No existing user database was used.
- Packaged JAR CLI: feasible example produces 3 defenses/4 visited nodes; infeasible example exits 2 with no partial schedule. CSV export and refusal to overwrite are behavior-tested.
- Repeated packaging with pinned output timestamps produced identical SHA-256 hashes (see work log). This checks same-environment artifact reproducibility, not universal byte identity across all JDK vendors/operating systems.
- Native Swing execution on macOS, using a temporary `jpackage --type app-image` bundle to expose the window to the UI automation tool. Selected both example radio buttons and generated each schedule; observed 3 rows/Feasible and 0 rows/Infeasible with export disabled. Initial empty state was observed. Inspected screenshots and corrected clipped sidebar text and viewport width. Final captures: `figures/desktop-feasible.png` and `figures/desktop-infeasible.png`.
- The initial bare Java process could not be selected by the UI tool. Packaging resolved this. Native CSV save/open dialogs, resize extremes, screen-reader speech, loading transients and keyboard-only completion were **not** exercised; their presence in code is not a manual-test claim.

## Test-first evidence and analysis

- Wrote scheduling specifications before the solver; observed missing types, then **three failing behavior assertions** against a deliberately empty/infeasible solver, then implemented backtracking.
- Wrote table model regression tests and observed `UnsupportedOperationException` with immutable input and `NullPointerException` on optional relationships before fixes.
- File/CLI and presentation API tests were introduced before those APIs (initial compilation failures). Real PostgreSQL round-trips subsequently validated persistence. This record does not claim every line was developed through a prior failing behavior assertion.
- **crapper** with current JaCoCo data: every detected method below CRAP 10; highest is recursive search at **8.0**. The UI has low measured coverage; splitting handlers improved complexity without inventing UI coverage.
- **dryer --threshold 0.8**: initially found duplicate entity table switches; shared column definitions removed them. Final report: **no duplicate candidates**.
- **mutator** on `domain`, using `SchedulerTest`: initially **23/25** detected mutation sites killed; blank-identifier and non-positive-budget tests killed the remaining two. Final snapshot: **25 killed, 0 survived, 0 uncovered detected sites**. This is the tool's detected-site set, not a claim that all possible mutations or record constructor checks were measured. No mutation claim is made for the entire application.
- Coverage reports are generated in `target/site/jacoco`; raw local analyzer reports are under ignored `.metrics/`.

## Environment incident

The disk briefly ran out of space and OrbStack stopped with an I/O/full-disk alert. An intermediate rerun failed to connect to PostgreSQL. After disk space returned, `orbctl start` and `docker compose up -d --wait` recovered the existing project database, and the full 20-test suite passed again. No global SDK changes, license acceptance, database reset or deletion of other projects was performed.

## Scope limits

The scheduler proves feasibility/infeasibility only over supplied finite candidates and fixed committees. It has no global optimization objective, examiner selection or fairness model. Legacy data was not migrated or sampled; date/room translation to the integer-day JPA model is intentionally explicit and unimplemented. See README for supported constraints and migration guidance. CI execution is tracked on GitHub for the pushed commit; the badge references the actual `ci.yml` workflow on `master`.

## Clean Architecture follow-up

- `JAVA_HOME=... COMMITTEE_DB_TEST=true ... ./mvnw spotless:apply clean verify`: **38 tests, 0 failures, 0 errors, 0 skips**, on Java 21/Maven Wrapper 3.9.11 with the dedicated PostgreSQL database. The subsequent `verify` formatting check confirmed all 63 Java source/test files were formatted.
- Four ArchUnit rules ran against compiled production classes: inward domain/application dependencies, no input-to-output implementation dependencies and no output-to-input dependencies. Existing CLI/static file entry methods remain behavior-tested compatibility facades.
- Application orchestration tests were introduced first and observed failing generation, source-failure and export assertions against no-op implementations before use cases were implemented. Table presentation was extracted behind a headless row/column/reset test. Parser tests added after mutation analysis cover identifiers, trailing empty values, exact one-MiB acceptance, oversized rejection, column counts and row diagnostics.
- `crapper --use-existing-coverage --threshold 10` passed; highest detected method CRAP **8.0**. `dryer --threshold 0.8` reported **no duplicate candidates** after the schedule table reused the generic read-only column model.
- Expanded mutation scope: domain, application and file adapters. Initial tool result: **53 killed, 6 survived, 3 uncovered**, across 62 detected sites. After adding meaningful parser-boundary/diagnostic assertions, final result: **62 killed, 0 survived, 0 uncovered detected sites**. This does not claim coverage of all possible mutations, record constructor checks or the entire GUI/persistence subsystem.
- Rendered all four Markdown Mermaid diagrams with Mermaid CLI 11.12.0 and an installed Chrome in a temporary headless rendering process; visually inspected dependency, use-case, domain and legacy diagrams. Mermaid remains embedded in the documentation, requiring no diagram build tool to view on GitHub.
- Executed packaged CLI feasible/infeasible examples again; exit statuses and synthetic assignments were preserved. Launched the refactored Swing application through a temporary native bundle and observed initial state, three feasible rows, switching dataset and infeasible state with export disabled. Native open/save dialogs and keyboard-only completion were not re-tested.
- No schema, credentials, entity package names or existing-database data were migrated. The runtime dependency set is unchanged; ArchUnit is test-only and Spotless/Google Java Format are build tools.
