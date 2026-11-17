package com.kleberandrade.committeetimetable.app;

import com.kleberandrade.committeetimetable.domain.*;
import com.kleberandrade.committeetimetable.io.PlanFiles;
import java.io.PrintStream;
import java.nio.file.*;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {}
    public static void main(String[] args) {
        if (java.util.Arrays.equals(args,new String[]{"--gui"})) { SwingUtilities.invokeLater(PlannerWindow::open); return; }
        System.exit(run(args,System.out,System.err));
    }
    public static int run(String[] args, PrintStream out, PrintStream err) {
        if (help(args)) {
            out.println("Committee Timetable\nUsage: java -jar <app.jar> --gui | --input <directory> [--output <new.csv>]\nExamples: --input examples/feasible | --input examples/infeasible\nExit: 0 feasible/help; 1 input error; 2 infeasible; 3 search limit.");
            return 0;
        }
        try {
            validate(args);
            var request = PlanFiles.read(Path.of(args[1]));
            var result = new Scheduler().solve(request,100_000);
            out.printf("%s · %d defenses · %d search nodes%n",result.status(),result.assignments().size(),result.visited());
            for (var a : result.assignments()) out.printf("%s | %s %s–%s | %s | %s%n",a.defense().id(),a.slot().window().day(),a.slot().window().start(),a.slot().window().end(),a.slot().room(),a.defense().title());
            export(args,result);
            return exitCode(result.status());
        } catch (Exception error) { err.println("Error: " + error.getMessage()); return 1; }
    }
    private static boolean help(String[] args) {
        return args.length == 0 || java.util.Arrays.equals(args,new String[]{"--help"});
    }
    private static void validate(String[] args) {
        if (args.length != 2 && args.length != 4) throw new IllegalArgumentException("Use --input <directory> [--output <new.csv>]");
        if (!args[0].equals("--input")) throw new IllegalArgumentException("Expected --input <directory>");
        if (args.length == 4 && !args[2].equals("--output")) throw new IllegalArgumentException("Expected --output <new.csv>");
    }
    private static void export(String[] args, Planning.Result result) throws java.io.IOException {
        if (args.length != 4 || result.status() != Planning.Status.FEASIBLE) return;
        Path output = Path.of(args[3]);
        if (Files.exists(output)) throw new IllegalArgumentException("Output already exists: " + output);
        PlanFiles.write(output,result);
    }
    private static int exitCode(Planning.Status status) {
        return switch (status) { case FEASIBLE -> 0; case INFEASIBLE -> 2; case LIMIT_REACHED -> 3; };
    }
}
