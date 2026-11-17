package com.kleberandrade.committeetimetable.app;

import com.kleberandrade.committeetimetable.bootstrap.CompositionRoot;

/** Compatibility entry point; dependency construction belongs to the composition root. */
public final class PlannerWindow {
  private PlannerWindow() {}

  public static void open() {
    CompositionRoot.openDesktop();
  }
}
