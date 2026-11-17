package com.kleberandrade.committeetimetable.adapters.in.cli;

import com.kleberandrade.committeetimetable.application.*;
import com.kleberandrade.committeetimetable.domain.Planning;
import java.io.PrintStream;

/** Console presentation; all scheduling and export policy belongs to use cases. */
public final class CommandLine {
  private final GenerateSchedule generateSchedule;
  private final ExportSchedule exportSchedule;
  private final PrintStream out;
  private final PrintStream err;

  public CommandLine(
      GenerateSchedule generateSchedule,
      ExportSchedule exportSchedule,
      PrintStream out,
      PrintStream err) {
    this.generateSchedule = generateSchedule;
    this.exportSchedule = exportSchedule;
    this.out = out;
    this.err = err;
  }

  public int run(String[] args) {
    if (help(args)) {
      out.println(
          "Committee Timetable\nUsage: java -jar <app.jar> --gui | --input <directory> [--output <new.csv>]\nExamples: --input examples/feasible | --input examples/infeasible\nExit: 0 feasible/help; 1 input error; 2 infeasible; 3 search limit.");
      return 0;
    }
    try {
      validate(args);
      var result = generateSchedule.execute(args[1]);
      out.printf(
          "%s · %d defenses · %d search nodes%n",
          result.status(), result.assignments().size(), result.visited());
      for (var a : result.assignments())
        out.printf(
            "%s | %s %s–%s | %s | %s%n",
            a.defense().id(),
            a.slot().window().day(),
            a.slot().window().start(),
            a.slot().window().end(),
            a.slot().room(),
            a.defense().title());
      export(args, result);
      return exitCode(result.status());
    } catch (RuntimeException error) {
      err.println("Error: " + error.getMessage());
      return 1;
    }
  }

  private static boolean help(String[] args) {
    return args.length == 0 || java.util.Arrays.equals(args, new String[] {"--help"});
  }

  private static void validate(String[] args) {
    if (args.length != 2 && args.length != 4)
      throw new IllegalArgumentException("Use --input <directory> [--output <new.csv>]");
    if (!args[0].equals("--input"))
      throw new IllegalArgumentException("Expected --input <directory>");
    if (args.length == 4 && !args[2].equals("--output"))
      throw new IllegalArgumentException("Expected --output <new.csv>");
  }

  private void export(String[] args, Planning.Result result) {
    if (args.length != 4 || result.status() != Planning.Status.FEASIBLE) return;
    exportSchedule.execute(args[3], result, ExportMode.CREATE_NEW);
  }

  private static int exitCode(Planning.Status status) {
    return switch (status) {
      case FEASIBLE -> 0;
      case INFEASIBLE -> 2;
      case LIMIT_REACHED -> 3;
    };
  }
}
