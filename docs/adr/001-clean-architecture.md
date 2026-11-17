# ADR-001: Introduce application use cases and dependency-owned ports

- Status: accepted implementation of the requested Clean Architecture refactoring; reviewable in this repository.
- Date: 2026-11-17

## Context

CLI and Swing both directly loaded files, constructed the solver and selected a search budget. Export eligibility/overwrite behavior could diverge. The domain was already framework-independent, while a historical JPA subsystem had different day/room semantics. The application must retain its existing executable/CLI contracts and useful legacy source entry methods.

## Decision

Introduce `GenerateSchedule` and `ExportSchedule`, application-owned `ScheduleSource`/`ScheduleExporter` ports and a domain-owned `ScheduleSolver` contract. Inject concrete dependencies in `CompositionRoot`. Place CLI/Swing under inbound adapters and TSV/CSV under outbound adapters. Keep location identifiers opaque to use cases; boundary failures preserve their cause. Use immutable domain records directly rather than duplicating them into transport DTOs.

Keep the public legacy JPA packages and supported static entry methods as a compatibility boundary. Do not introduce an implicit mapping between legacy integer days and calendar dates, or pretend committees already have rooms. Enforce source dependency rules with ArchUnit and consistent Java formatting with Spotless during verification.

## Alternatives considered

- Keep orchestration duplicated in both interfaces: fewer classes now, but policy and integration changes remain coupled to presentation.
- Introduce Spring or another dependency injection framework: unnecessary runtime dependency for a single desktop/CLI application; constructor injection is sufficient.
- Split into many Maven modules: stronger build isolation, but additional publishing/build overhead is not justified at current size. Compiled dependency tests provide a practical boundary now.
- Rename every JPA class/package and map it into the scheduler: risks changing compatibility and data semantics without a defined migration. Preserve those APIs until an explicit persistence use case requires translation.

## Consequences

Use cases can run entirely with in-memory adapters in tests; both user interfaces share scheduling/export policy. Filesystem safety is handled by a concrete adapter, including atomic create-new output. Adding a source or solver changes an adapter/wiring and its tests.

There are additional small classes and compatibility delegates. A single Maven module does not provide runtime isolation. The JPA subsystem remains separate from the file scheduler, and no persistence integration is implied by the architecture diagrams. An optimizing algorithm still requires its own objective/guarantee specification.

## Validation

`PlanningUseCasesTest` pins result/error/export behavior through ports. `DependencyRulesTest` checks four compiled dependency rules. Existing solver, file/CLI, presentation, table-model and PostgreSQL tests pin prior behavior. Spotless fails verification on format drift. The actual executed results are recorded in `docs/VERIFICATION.md` and CI for the pushed commit.
