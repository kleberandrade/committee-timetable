# Articles and primary references

The relevant sections of these references were opened and read for this documentation update on 2026-11-17. They are external works under their authors' own terms; this repository links to them and does not republish or relicense them. The two local articles document this implementation rather than reproducing those works.

## Read this repository first

1. [Clean Architecture in Committee Timetable](clean-architecture.md): dependency ownership, use cases, adapters, compatibility and extension paths.
2. [Solving the defense scheduling problem](scheduling-solution.md): model, constraints, candidate filtering, backtracking trace, result guarantees and complexity limits.
3. [Architecture and class diagrams](../architecture.md): source dependency direction, scheduling/domain classes and the separate legacy JPA model.

## External reading

- **Robert C. Martin — [The Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html), 13 August 2012.** Background for inward dependencies and independent business rules. Applied here through application-owned ports, injected use cases and compiled dependency tests.
- **Alistair Cockburn — [Hexagonal architecture](https://alistair.cockburn.us/hexagonal-architecture), original article from 2005.** Background for driving the same application through tests and different external adapters. Applied here to the shared CLI/Swing use cases and replaceable dataset/export boundaries.
- **UC Berkeley CS 188 — [Solving Constraint Satisfaction Problems](https://inst.eecs.berkeley.edu/~cs188/textbook/csp/solving.html).** Background for conflict-aware backtracking. Applied here to finite room/start assignments and undoing unsuccessful prefixes.
- **UC Berkeley CS 188 — [Ordering](https://inst.eecs.berkeley.edu/~cs188/textbook/csp/ordering.html).** Explains MRV/LCV and more advanced ordering. This project uses a static initial candidate-count ordering; dynamic MRV and LCV are not implemented.
- **TNG Technology Consulting — [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html).** Official reference for rules over compiled Java classes. See `DependencyRulesTest` for the four enforced dependency boundaries.
- **Hibernate — [ORM 7.4 User Guide](https://docs.hibernate.org/orm/7.4/userguide/html_single/).** Official reference for the separate persistence compatibility subsystem, entity mappings, transactions and validation. JPA entities are not passed into the scheduling use cases.

## Medium authorship

No linked Medium article or shortened tutorial URL was present in the original README/sources. These references are therefore explicitly attributed to their actual authors; none is presented as a Medium article by the repository author. No unrelated account article was edited or published. The [Medium review record](../medium-update.pt-BR.md) preserves that distinction.
