package com.kleberandrade.committeetimetable.domain;

import com.kleberandrade.committeetimetable.domain.Planning.*;

@FunctionalInterface
public interface ScheduleSolver {
  Result solve(Request request, long nodeLimit);
}
