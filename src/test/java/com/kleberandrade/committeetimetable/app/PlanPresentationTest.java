package com.kleberandrade.committeetimetable.app;

import static org.junit.jupiter.api.Assertions.*;

import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlanPresentationTest {
  @Test
  void diagnosticsDistinguishEmptyInfeasibleAndUnknownFeasibility() {
    assertTrue(
        PlanPresentation.message(new Result(Status.FEASIBLE, List.of(), 1))
            .contains("no defenses"));
    assertTrue(
        PlanPresentation.message(new Result(Status.INFEASIBLE, List.of(), 3))
            .contains("No complete schedule"));
    assertTrue(
        PlanPresentation.message(new Result(Status.LIMIT_REACHED, List.of(), 100))
            .contains("Feasibility remains unknown"));
    assertEquals("Feasible", PlanPresentation.title(Status.FEASIBLE));
    assertEquals("Infeasible", PlanPresentation.title(Status.INFEASIBLE));
    assertEquals("Search limit", PlanPresentation.title(Status.LIMIT_REACHED));
  }
}
