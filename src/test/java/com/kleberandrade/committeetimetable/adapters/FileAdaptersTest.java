package com.kleberandrade.committeetimetable.adapters;

import static org.junit.jupiter.api.Assertions.*;

import com.kleberandrade.committeetimetable.adapters.out.file.*;
import com.kleberandrade.committeetimetable.application.*;
import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileAdaptersTest {
  @TempDir Path directory;
  private final Result complete = new Result(Status.FEASIBLE, List.of(), 1);

  @Test
  void createNewPreservesExistingContentsAndReportsTheFilesystemFailure() throws Exception {
    Path output = directory.resolve("schedule.csv");
    Files.writeString(output, "keep this existing file");
    var failure =
        assertThrows(
            PlanningException.class,
            () ->
                new CsvScheduleExporter()
                    .write(output.toString(), complete, ExportMode.CREATE_NEW));
    assertInstanceOf(FileAlreadyExistsException.class, failure.getCause());
    assertEquals("keep this existing file", Files.readString(output));
  }

  @Test
  void replaceModeIsExplicitAndTruncatesPriorContents() throws Exception {
    Path output = directory.resolve("schedule.csv");
    Files.writeString(output, "old content that should be replaced");
    new CsvScheduleExporter().write(output.toString(), complete, ExportMode.REPLACE_EXISTING);
    assertEquals(
        List.of("defense,title,date,start,end,room,professors,students"),
        Files.readAllLines(output));
  }

  @Test
  void missingInputRetainsTheOriginalCause() {
    var failure =
        assertThrows(
            PlanningException.class,
            () -> new TsvScheduleSource().load(directory.resolve("missing").toString()));
    assertInstanceOf(NoSuchFileException.class, failure.getCause());
    assertTrue(failure.getMessage().contains("missing"));
  }

  private void copyExample() throws Exception {
    for (String name : List.of("defenses.tsv", "slots.tsv", "availability.tsv")) {
      Files.copy(Path.of("examples/feasible", name), directory.resolve(name));
    }
  }

  @Test
  void keepsDefenseIdentifiersDistinctFromTitles() {
    var request = new TsvScheduleSource().load("examples/feasible");
    assertEquals("D01", request.defenses().getFirst().id());
    assertEquals("Water, safely", request.defenses().getFirst().title());
  }

  @Test
  void rejectsTrailingEmptyParticipantsAndRetainsRowDiagnostics() throws Exception {
    copyExample();
    Path defenses = directory.resolve("defenses.tsv");
    Files.writeString(defenses, Files.readString(defenses).replace("S01\n", "S01,\n"));
    var failure =
        assertThrows(
            IllegalArgumentException.class,
            () -> new TsvScheduleSource().load(directory.toString()));
    assertTrue(failure.getMessage().contains("defenses.tsv:2:"));
    assertTrue(failure.getMessage().contains("must not be blank"));
  }

  @Test
  void reportsWrongColumnCountWithTheFileAndRow() throws Exception {
    copyExample();
    Path defenses = directory.resolve("defenses.tsv");
    String header = Files.readAllLines(defenses).getFirst();
    Files.writeString(defenses, header + "\nD01\tIncomplete\n");
    var failure =
        assertThrows(
            IllegalArgumentException.class,
            () -> new TsvScheduleSource().load(directory.toString()));
    assertTrue(failure.getMessage().endsWith("defenses.tsv:2: Expected 6 tab-separated columns"));
  }

  @Test
  void diagnosesAnEmptyLastFieldAsAnInvalidValueRatherThanAMissingColumn() throws Exception {
    copyExample();
    Path defenses = directory.resolve("defenses.tsv");
    String header = Files.readAllLines(defenses).getFirst();
    Files.writeString(defenses, header + "\nD01\tProject\t60\tP01\tP01,P02,P03\t\n");
    var failure =
        assertThrows(
            IllegalArgumentException.class,
            () -> new TsvScheduleSource().load(directory.toString()));
    assertTrue(failure.getMessage().contains("defenses.tsv:2:"));
    assertTrue(failure.getMessage().contains("must not be blank"));
  }

  @Test
  void acceptsExactlyOneMiBAndRejectsOneByteMore() throws Exception {
    copyExample();
    Path defenses = directory.resolve("defenses.tsv");
    String prefix = Files.readAllLines(defenses).getFirst() + "\nD01\t";
    String suffix = "\t60\tP01\tP01,P02,P03\tS01\n";
    String title = "A".repeat(1_048_576 - prefix.length() - suffix.length());
    Files.writeString(defenses, prefix + title + suffix);
    assertEquals(1_048_576, Files.size(defenses));
    assertEquals(
        "D01", new TsvScheduleSource().load(directory.toString()).defenses().getFirst().id());
    Files.writeString(defenses, "A", StandardOpenOption.APPEND);
    var failure =
        assertThrows(
            IllegalArgumentException.class,
            () -> new TsvScheduleSource().load(directory.toString()));
    assertTrue(failure.getMessage().endsWith("defenses.tsv: file exceeds 1 MiB"));
  }
}
