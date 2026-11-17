package com.kleberandrade.committeetimetable.app;

import com.kleberandrade.committeetimetable.bootstrap.CompositionRoot;
import java.io.PrintStream;
import java.util.Arrays;
import javax.swing.SwingUtilities;

/** Stable executable entry point. No scheduling, filesystem or export policy lives here. */
public final class Main {
  private Main() {}

  public static void main(String[] args) {
    if (Arrays.equals(args, new String[] {"--gui"})) {
      SwingUtilities.invokeLater(CompositionRoot::openDesktop);
      return;
    }
    System.exit(run(args, System.out, System.err));
  }

  public static int run(String[] args, PrintStream out, PrintStream err) {
    return CompositionRoot.commandLine(out, err).run(args);
  }
}
