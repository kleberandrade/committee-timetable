package com.kleberandrade.committeetimetable.io;
import java.nio.file.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import com.kleberandrade.committeetimetable.domain.*;
class PlanFilesTest {
    @TempDir Path dir;
    @Test void readsSyntheticExampleAndWritesAnEscapedSchedule() throws Exception {
        var request = PlanFiles.read(Path.of("examples/feasible"));
        var result = new Scheduler().solve(request,100_000);
        assertEquals(Planning.Status.FEASIBLE,result.status());
        assertEquals(3,result.assignments().size());
        Path csv = dir.resolve("result.csv"); PlanFiles.write(csv,result);
        assertTrue(Files.readString(csv).contains("\"Water, safely\""));
        assertEquals(4,Files.readAllLines(csv).size());
    }
    @Test void rejectsWrongHeadersAndReportsFileAndLine() throws Exception {
        Files.writeString(dir.resolve("defenses.tsv"),"bad header\n");
        var error = assertThrows(IllegalArgumentException.class,()->PlanFiles.read(dir));
        assertTrue(error.getMessage().contains("defenses.tsv:1"));
    }
    @Test void rejectsDuplicateCommitteeMembersRatherThanSilentlyCollapsing() throws Exception {
        for (String name : new String[]{"defenses.tsv","slots.tsv","availability.tsv"}) Files.copy(Path.of("examples/feasible",name),dir.resolve(name));
        var path = dir.resolve("defenses.tsv");
        Files.writeString(path,Files.readString(path).replace("P01,P02,P03","P01,P01,P03"));
        assertThrows(IllegalArgumentException.class,()->PlanFiles.read(dir));
    }
}
