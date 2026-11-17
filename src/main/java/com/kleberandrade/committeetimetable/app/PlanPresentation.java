package com.kleberandrade.committeetimetable.app;

import com.kleberandrade.committeetimetable.domain.Planning.*;

/** Compatibility facade for the desktop presenter. */
public final class PlanPresentation {
  private PlanPresentation() {}

  public static String title(Status status) {
    return com.kleberandrade.committeetimetable.adapters.in.swing.PlanPresentation.title(status);
  }

  public static String message(Result result) {
    return com.kleberandrade.committeetimetable.adapters.in.swing.PlanPresentation.message(result);
  }
}
