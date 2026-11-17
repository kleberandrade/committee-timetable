# Clean Architecture in Committee Timetable

## The problem being addressed

A pure solver alone does not separate an application cleanly. Previously, both the command line and the Swing worker opened TSV files, instantiated `Scheduler` and chose a literal search budget. Export policy was also spread between interfaces. Replacing the file input or changing the budget meant editing presentation code.

The new application layer makes these decisions explicit. `GenerateSchedule` receives a `ScheduleSource`, a `ScheduleSolver` and a positive budget. Its only operation loads a validated request and invokes the solver. `ExportSchedule` receives a `ScheduleExporter` and rejects incomplete/infeasible results before any export happens. The constructor dependencies state what each use case needs; there is no service locator or framework container.

## What depends on what

The [class diagrams](../architecture.md) show the actual contracts. In production, `TsvScheduleSource` supplies requests and `CsvScheduleExporter` serializes results. `Scheduler` implements the domain-owned `ScheduleSolver` interface. `CommandLine` and `PlannerWindow` receive application use cases and present their results. The `CompositionRoot` selects concrete implementations once.

The [dependency rule](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) motivates keeping external mechanisms out of the inner layers. [Cockburn's original ports-and-adapters article](https://alistair.cockburn.us/hexagonal-architecture) explains why automated tests and user interfaces can drive the same application boundary. Here, those principles are enforced by compiled-class tests rather than package names alone.

For example, `PlanningUseCasesTest` supplies a source that returns an immutable request without reading a disk or starting PostgreSQL. The same use case is injected into the production Swing worker. This is a useful replacement boundary: a future database-backed **scheduling** source would implement `ScheduleSource`, perform an explicit mapping to `Planning.Request`, and be selected in `CompositionRoot`.

## Clean Code choices made in this repository

- **One reason to change per boundary.** TSV parsing, CSV encoding, console presentation, desktop presentation and application policy live in separate classes/packages.
- **A single policy location.** `GenerateSchedule.DEFAULT_NODE_LIMIT` defines the shipped budget and the composition root injects it. `ExportSchedule` owns the rule that only complete feasible plans may be exported.
- **Validated immutable input.** The domain rejects invalid committees, durations, time windows, missing availability, duplicate defense IDs and oversized instances at construction. Search operates on those values instead of repeatedly parsing strings.
- **Explicit failures.** File adapters preserve causes in `PlanningException`; CLI maps failure to its documented exit code, while Swing displays a diagnostic. A failed read never becomes a fabricated empty feasible plan.
- **Safe output semantics.** CLI selects `CREATE_NEW`. The CSV adapter uses the filesystem's atomic create-new operation instead of a check-then-write sequence. Swing asks before choosing `REPLACE_EXISTING`.
- **Small, meaningful interfaces.** Ports represent input loading, solving and exporting. Concrete use-case methods already form the input boundary; extra interfaces, DTO copies and generic base services would add ceremony without solving a current problem.
- **Consistent reviewable formatting.** Spotless with pinned Google Java Format runs during `verify`, including in CI. `./mvnw spotless:apply` fixes formatting; format drift fails the build.

## Why the legacy JPA entities were not renamed into domain objects

Legacy `Committee`, `Project`, `Professor`, `Student`, `Schedule` and related classes retain JPA annotations and mutable getters/setters. They belong to the persistence compatibility boundary. The scheduling core has different semantics: calendar dates, explicit rooms, fixed three-professor composition and immutable values.

Pretending those representations were interchangeable would hide a migration decision. In particular, legacy integer days do not define a calendar date, and committee rows have no room. The application therefore does not automatically persist a generated CSV schedule. That adapter requires an agreed day/date convention and a schema migration. Existing JPA packages, tables and CRUD interfaces remain available and are integration-tested independently.

## How to extend the application

To introduce another dataset source, implement `ScheduleSource` in `adapters.out`, validate/translate external data there and return `Planning.Request`. Test the adapter separately, then substitute it in `CompositionRoot`. Existing use-case, domain and CLI/Swing behavior tests should continue to pass.

To introduce a different search strategy, implement `ScheduleSolver` while respecting the result statuses and no-partial-result contract. Add behavior tests against the same constraints before selecting the strategy in wiring. An optimizing solver would need an explicit objective and documented optimality/termination guarantees; changing the implementation name is not sufficient.

To add another user interface, call the existing use cases from a new input adapter. Keep request serialization, threading and display state in that adapter, and leave conflict/composition rules in the domain.

## Trade-offs

This remains a single Maven module and a small desktop/CLI application. ArchUnit enforces package boundaries, but does not provide module-level classloader isolation. Compatibility facades preserve supported entry methods at the cost of a few delegates. The application passes immutable domain values directly rather than maintaining duplicate DTO hierarchies. These choices keep the dependency rule useful without turning a teaching project into a distributed system.
