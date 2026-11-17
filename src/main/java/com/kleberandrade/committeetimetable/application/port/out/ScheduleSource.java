package com.kleberandrade.committeetimetable.application.port.out;

import com.kleberandrade.committeetimetable.domain.Planning.Request;

/** Location is an opaque adapter-specific identifier, not a filesystem dependency. */
@FunctionalInterface
public interface ScheduleSource {
  Request load(String location);
}
