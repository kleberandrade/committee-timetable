# Modernization work log

- Inspected clean `master` at c707b83 and the real sources. No application entry point, scheduling algorithm, tests or Medium/tutorial links exist in the tracked material. Preserved MIT attribution and ER diagram.
- Found broken persistence class/provider names, embedded credentials, mixed JPA providers, swallowed DAO errors, wrong ProjectTypeDAO generic and table event/null problems.
- Selected Java 21, Maven 3.9.11, Hibernate 7.4.16/Jakarta Persistence 3.2 and PostgreSQL JDBC 42.7.14 after checking published artifacts. Preserve entity/property semantics; do not auto-update an existing database.
- Plan verification in slices: failing domain behavior tests, finite-slot solver, boundary tests, DAO/database integration, desktop/headless demonstrations and CI. Separate scheduling value objects from mutable JPA entities.
