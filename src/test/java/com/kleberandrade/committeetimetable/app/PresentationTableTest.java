package com.kleberandrade.committeetimetable.app;

import static org.junit.jupiter.api.Assertions.*;

import com.kleberandrade.committeetimetable.domain.Scheduler;
import com.kleberandrade.committeetimetable.io.PlanFiles;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PresentationTableTest {
  @Test
  void feasibleScheduleExplainsSupportedGuaranteesAndSearchBudget() throws Exception {
    var result = new Scheduler().solve(PlanFiles.read(Path.of("examples/feasible")), 100_000);
    assertTrue(PlanPresentation.message(result).contains("All defenses scheduled"));
    assertTrue(PlanPresentation.message(result).contains("not a global optimum"));
    assertTrue(PlanPresentation.message(result).contains("Search nodes: " + result.visited()));
  }
}
