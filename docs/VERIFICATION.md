# Verification record — 2026-11-17

## Executed locally

- Java 21 (Homebrew OpenJDK), Maven 3.9.11 via the generated Wrapper, macOS.
- `./mvnw clean verify` and subsequent `verify` after desktop corrections: **19 tests, zero failures/errors/skips** with `COMMITTEE_DB_TEST=true` and the isolated PostgreSQL running. Seven test classes cover scheduling, file boundaries/CSV, CLI behavior, presentation text, Swing table models and persistence.
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

The disk briefly ran out of space and OrbStack stopped with an I/O/full-disk alert. An intermediate rerun failed to connect to PostgreSQL. After disk space returned, `orbctl start` and `docker compose up -d --wait` recovered the existing project database, and the full 19-test suite passed again. No global SDK changes, license acceptance, database reset or deletion of other projects was performed.

## Scope limits

The scheduler proves feasibility/infeasibility only over supplied finite candidates and fixed committees. It has no global optimization objective, examiner selection or fairness model. Legacy data was not migrated or sampled; date/room translation to the integer-day JPA model is intentionally explicit and unimplemented. See README for supported constraints and migration guidance. CI execution is tracked on GitHub for the pushed commit; the badge references the actual `ci.yml` workflow on `master`.
