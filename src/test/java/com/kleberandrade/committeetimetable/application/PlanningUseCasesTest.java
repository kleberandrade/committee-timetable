package com.kleberandrade.committeetimetable.application;

import static org.junit.jupiter.api.Assertions.*;

import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PlanningUseCasesTest {
  private final Request empty = new Request(List.of(), List.of(), Map.of());

  @Test
  void generatesThroughPortsWithTheConfiguredBudget() {
    var useCase =
        new GenerateSchedule(
            location -> empty,
            (request, limit) -> new Result(Status.FEASIBLE, List.of(), limit),
            42);
    assertEquals(new Result(Status.FEASIBLE, List.of(), 42), useCase.execute("synthetic"));
  }

  @Test
  void propagatesSourceFailureWithoutInventingASchedule() {
    var useCase =
        new GenerateSchedule(
            location -> {
              throw new PlanningException("Unreadable dataset");
            },
            (request, limit) -> {
              throw new AssertionError("No input to solve");
            },
            42);
    assertEquals(
        "Unreadable dataset",
        assertThrows(PlanningException.class, () -> useCase.execute("missing")).getMessage());
  }

  @Test
  void rejectsNonPositiveBudget() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new GenerateSchedule(location -> empty, (request, limit) -> null, 0));
  }

  @Test
  void exportsOnlyCompleteFeasibleSchedules() {
    var written = new AtomicReference<Result>();
    var useCase = new ExportSchedule((destination, result, mode) -> written.set(result));
    var feasible = new Result(Status.FEASIBLE, List.of(), 1);
    useCase.execute("schedule.csv", feasible, ExportMode.CREATE_NEW);
    assertEquals(feasible, written.get());
    written.set(null);
    for (Status status : List.of(Status.INFEASIBLE, Status.LIMIT_REACHED)) {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              useCase.execute(
                  "schedule.csv", new Result(status, List.of(), 1), ExportMode.CREATE_NEW));
      assertNull(written.get());
    }
  }
}
