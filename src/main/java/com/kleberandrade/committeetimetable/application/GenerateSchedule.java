package com.kleberandrade.committeetimetable.application;

import com.kleberandrade.committeetimetable.application.port.out.ScheduleSource;
import com.kleberandrade.committeetimetable.domain.Planning.Result;
import com.kleberandrade.committeetimetable.domain.ScheduleSolver;
import java.util.Objects;

/** Coordinates input and solving without knowing about files, Swing, CLI or persistence. */
public final class GenerateSchedule {
  public static final long DEFAULT_NODE_LIMIT = 100_000;
  private final ScheduleSource source;
  private final ScheduleSolver solver;
  private final long nodeLimit;

  public GenerateSchedule(ScheduleSource source, ScheduleSolver solver, long nodeLimit) {
    this.source = Objects.requireNonNull(source);
    this.solver = Objects.requireNonNull(solver);
    if (nodeLimit < 1) throw new IllegalArgumentException("Node limit must be positive");
    this.nodeLimit = nodeLimit;
  }

  public Result execute(String location) {
    return solver.solve(source.load(location), nodeLimit);
  }
}
