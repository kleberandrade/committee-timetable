package com.kleberandrade.committeetimetable.bootstrap;

import com.kleberandrade.committeetimetable.adapters.in.cli.CommandLine;
import com.kleberandrade.committeetimetable.adapters.in.swing.PlannerWindow;
import com.kleberandrade.committeetimetable.adapters.out.file.*;
import com.kleberandrade.committeetimetable.application.*;
import com.kleberandrade.committeetimetable.domain.Scheduler;
import java.io.PrintStream;

/**
 * The only wiring point that chooses concrete scheduling adapters and the default search policy.
 */
public final class CompositionRoot {
  private CompositionRoot() {}

  private static GenerateSchedule generateSchedule() {
    return new GenerateSchedule(
        new TsvScheduleSource(), new Scheduler(), GenerateSchedule.DEFAULT_NODE_LIMIT);
  }

  private static ExportSchedule exportSchedule() {
    return new ExportSchedule(new CsvScheduleExporter());
  }

  public static CommandLine commandLine(PrintStream out, PrintStream err) {
    return new CommandLine(generateSchedule(), exportSchedule(), out, err);
  }

  public static void openDesktop() {
    PlannerWindow.open(generateSchedule(), exportSchedule());
  }
}
