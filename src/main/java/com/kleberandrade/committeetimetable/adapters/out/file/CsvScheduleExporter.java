package com.kleberandrade.committeetimetable.adapters.out.file;

import com.kleberandrade.committeetimetable.adapters.shared.ParticipantFormatter;
import com.kleberandrade.committeetimetable.application.*;
import com.kleberandrade.committeetimetable.application.port.out.ScheduleExporter;
import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** CSV output adapter. CREATE_NEW enforces no-overwrite atomically at the filesystem boundary. */
public final class CsvScheduleExporter implements ScheduleExporter {
  @Override
  public void write(String destination, Result result, ExportMode mode) {
    try {
      write(Path.of(destination), result, mode);
    } catch (IOException failure) {
      throw new PlanningException("Could not export schedule: " + failure.getMessage(), failure);
    }
  }

  public static void write(Path path, Result result, ExportMode mode) throws IOException {
    var lines = new ArrayList<String>();
    lines.add("defense,title,date,start,end,room,professors,students");
    for (Assignment assignment : result.assignments()) {
      var defense = assignment.defense();
      var slot = assignment.slot();
      var window = slot.window();
      lines.add(
          String.join(
              ",",
              List.of(
                      defense.id(),
                      defense.title(),
                      window.day().toString(),
                      window.start().toString(),
                      window.end().toString(),
                      slot.room(),
                      ParticipantFormatter.joined(defense.professors()),
                      ParticipantFormatter.joined(defense.students()))
                  .stream()
                  .map(CsvScheduleExporter::quoteField)
                  .toList()));
    }
    OpenOption[] options =
        mode == ExportMode.CREATE_NEW
            ? new OpenOption[] {StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE}
            : new OpenOption[] {
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.WRITE
            };
    Files.write(path, lines, StandardCharsets.UTF_8, options);
  }

  private static String quoteField(String value) {
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }
}
