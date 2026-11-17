package com.kleberandrade.committeetimetable.io;

import com.kleberandrade.committeetimetable.adapters.out.file.*;
import com.kleberandrade.committeetimetable.adapters.shared.ParticipantFormatter;
import com.kleberandrade.committeetimetable.application.ExportMode;
import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/** Compatibility facade. New integrations should use the application use cases and ports. */
public final class PlanFiles {
  private PlanFiles() {}

  public static Request read(Path directory) throws IOException {
    return TsvScheduleSource.read(directory);
  }

  public static void write(Path path, Result result) throws IOException {
    CsvScheduleExporter.write(path, result, ExportMode.REPLACE_EXISTING);
  }

  public static String joined(Set<String> ids) {
    return ParticipantFormatter.joined(ids);
  }
}
