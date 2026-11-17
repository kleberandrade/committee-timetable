package com.kleberandrade.committeetimetable.adapters.shared;

import java.util.Set;
import java.util.TreeSet;

public final class ParticipantFormatter {
  private ParticipantFormatter() {}

  public static String joined(Set<String> ids) {
    return String.join(", ", new TreeSet<>(ids));
  }
}
