# Contributing

Use JDK 21 and the pinned Maven Wrapper. Keep the desktop/CLI contract and existing database semantics unless the change includes an explicit, reviewed migration.

## Choose the responsibility first

- Put composition, availability, time/conflict rules and search in `domain`. Depend only on basic Java language/collection/time classes and other domain types.
- Put orchestration and application policy in `application`. Accept dependencies through constructors. Define output ports there; keep filesystem paths, serialization, Swing, console streams and JPA out.
- Put console/desktop presentation in `adapters.in`, and external source/export implementations in `adapters.out`. Only `bootstrap.CompositionRoot` chooses concrete scheduling dependencies.
- Treat `model`, `dao`, `utils`, the legacy table models and static compatibility facades as outer compatibility code. JPA entities are not scheduling domain objects. Do not invent a day/date mapping or discard room information to connect them.

Introduce an interface when it represents a real replaceable boundary, not simply to mirror every class. Use descriptive domain names, immutable values, explicit constructor dependencies and focused methods. Validate untrusted values at their boundary. Preserve original causes when translating I/O failures; present errors in the input adapter instead of printing and hiding them.

## Verify behavior before changing structure

For a bug or new behavior, write the smallest meaningful failing test first and verify the failure reason. For a refactoring, pin the existing behavior before moving it. Use in-memory ports for application tests, real synthetic files for filesystem adapters, and the dedicated PostgreSQL service for persistence integration. Do not write tests that only repeat the implementation or claim UI/database tests that were not run.

```sh
./mvnw spotless:apply
./mvnw clean verify
```

`verify` enforces formatting and four compiled dependency rules as well as behavior tests. Database tests are explicitly skipped without `COMMITTEE_DB_TEST=true`; run the isolated database workflow from the README when changing persistence. Never point the integration-test environment at a production database.

For changes to parsing, test valid boundaries as well as malformed input and diagnostics. For CSV export, preserve `CREATE_NEW` no-overwrite semantics and require explicit replacement. For solving, distinguish exhausted search from an interrupted/budget-limited search and never return an unsuccessful partial plan as complete.

## Keep documentation synchronized

Update the [class diagrams](docs/architecture.md), relevant [technical article](docs/articles/references.md) and an [ADR](docs/adr/001-clean-architecture.md) when changing a boundary, constraint, guarantee or persistence representation. Mermaid source lives in Markdown and renders directly on GitHub. Link primary external references with their real authors; do not copy/relicense third-party articles or invent a Medium association.

Keep commits focused and explain the reason for behavior/architecture changes, the checks performed and meaningful limits. Include only synthetic participant data. Preserve MIT attribution and third-party license resources.
