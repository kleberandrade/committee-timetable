package com.kleberandrade.committeetimetable.application.port.out;

import com.kleberandrade.committeetimetable.application.ExportMode;
import com.kleberandrade.committeetimetable.domain.Planning.Result;

@FunctionalInterface
public interface ScheduleExporter {
  void write(String destination, Result result, ExportMode mode);
}
