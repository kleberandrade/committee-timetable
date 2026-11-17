package com.kleberandrade.committeetimetable.app;
import com.kleberandrade.committeetimetable.io.PlanFiles;
import com.kleberandrade.committeetimetable.domain.Scheduler;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PresentationTableTest {
    @Test void feasibleScheduleExplainsSupportedGuaranteesAndSearchBudget() throws Exception {
        var result = new Scheduler().solve(PlanFiles.read(Path.of("examples/feasible")),100_000);
        assertTrue(PlanPresentation.message(result).contains("All defenses scheduled"));
        assertTrue(PlanPresentation.message(result).contains("not a global optimum"));
        assertTrue(PlanPresentation.message(result).contains("Search nodes: " + result.visited()));
    }
}
