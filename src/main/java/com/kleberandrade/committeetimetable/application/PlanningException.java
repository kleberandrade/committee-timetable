package com.kleberandrade.committeetimetable.application;

/** Failure at an application boundary; the original cause remains available for diagnostics. */
public final class PlanningException extends RuntimeException {
  public PlanningException(String message) {
    super(message);
  }

  public PlanningException(String message, Throwable cause) {
    super(message, cause);
  }
}
