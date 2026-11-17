# Third-party notices

The project remains MIT, copyright 2018 Kleber de Oliveira Andrade. Maven downloads dependencies under their original licenses; the MIT license does not cover those libraries.

Runtime dependencies include Hibernate ORM 7.4 (Apache-2.0), Jakarta Persistence 3.2 (EPL-2.0 / EDL-1.0), PostgreSQL JDBC (BSD-2-Clause), Jakarta Transaction (EPL-2.0 / GPL-2.0 with Classpath Exception), JBoss Logging (Apache-2.0), Hibernate Models (Apache-2.0), Byte Buddy (Apache-2.0), Classmate (Apache-2.0), Jakarta XML Binding/JAXB runtime and supporting Jakarta APIs (their upstream EPL/EDL/BSD licenses), and ANTLR runtime (BSD-3-Clause). Test tooling includes JUnit (EPL-2.0) and JaCoCo (EPL-2.0). PostgreSQL container contents carry their own upstream licenses.

Consult each resolved artifact's POM, license and notices for authoritative terms. The executable dependency bundle preserves upstream META-INF license/notice resources, merging Apache notices where applicable. Re-distributors should review its contents and their distribution requirements. No third-party implementation has been copied into project sources.

See the [resolved runtime inventory](RUNTIME_LICENSES.md) for exact versions and upstream license URLs. Original per-artifact POMs and available embedded license/notice texts are preserved without name collisions under `META-INF/third-party/` in both built JARs.
