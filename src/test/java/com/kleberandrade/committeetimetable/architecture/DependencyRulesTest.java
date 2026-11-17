package com.kleberandrade.committeetimetable.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.*;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.kleberandrade.committeetimetable",
    importOptions = ImportOption.DoNotIncludeTests.class)
class DependencyRulesTest {
  @ArchTest
  static final ArchRule domainHasNoOuterDependencies =
      classes()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java.lang..", "java.util..", "java.time..", "..domain..");

  @ArchTest
  static final ArchRule applicationDependsOnlyInwards =
      classes()
          .that()
          .resideInAPackage("..application..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java.lang..", "java.util..", "..domain..", "..application..");

  @ArchTest
  static final ArchRule inputAdaptersUseCasesRatherThanInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..adapters.in..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..adapters.out..",
              "..bootstrap..",
              "com.kleberandrade.committeetimetable.io..",
              "..dao..",
              "..model..",
              "..utils..");

  @ArchTest
  static final ArchRule outputAdaptersCannotCallUserInterfaces =
      noClasses()
          .that()
          .resideInAPackage("..adapters.out..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapters.in..", "..app..", "..bootstrap..");
}
