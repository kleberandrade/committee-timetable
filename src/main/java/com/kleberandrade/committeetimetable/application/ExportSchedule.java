package com.kleberandrade.committeetimetable.application;

import com.kleberandrade.committeetimetable.application.port.out.ScheduleExporter;
import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.util.Objects;

/** Export policy is shared by all interfaces; incomplete schedules are never exported. */
public final class ExportSchedule {
  private final ScheduleExporter exporter;

  public ExportSchedule(ScheduleExporter exporter) {
    this.exporter = Objects.requireNonNull(exporter);
  }

  public void execute(String destination, Result result, ExportMode mode) {
    Objects.requireNonNull(result);
    Objects.requireNonNull(mode);
    if (result.status() != Status.FEASIBLE)
      throw new IllegalArgumentException("Only complete feasible schedules can be exported");
    exporter.write(destination, result, mode);
  }
}
