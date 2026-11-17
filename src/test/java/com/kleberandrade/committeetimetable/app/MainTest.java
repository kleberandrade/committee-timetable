package com.kleberandrade.committeetimetable.app;
import java.io.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MainTest {
    @org.junit.jupiter.api.io.TempDir java.nio.file.Path dir;
    @Test void cliExportsOnlyFeasibleSchedulesAndRefusesOverwrite() throws Exception {
        var out = new PrintStream(new ByteArrayOutputStream());
        var path = dir.resolve("plan.csv");
        assertEquals(0,Main.run(new String[]{"--input","examples/feasible","--output",path.toString()},out,out));
        String original = java.nio.file.Files.readString(path);
        assertTrue(original.contains("Water, safely"));
        assertEquals(1,Main.run(new String[]{"--input","examples/feasible","--output",path.toString()},out,out));
        assertEquals(original,java.nio.file.Files.readString(path));
        var impossible = dir.resolve("impossible.csv");
        assertEquals(2,Main.run(new String[]{"--input","examples/infeasible","--output",impossible.toString()},out,out));
        assertFalse(java.nio.file.Files.exists(impossible));
        assertEquals(1,Main.run(new String[]{"--bogus","examples/feasible"},out,out));
        assertEquals(1,Main.run(new String[]{"--input","examples/feasible","--wrong","x"},out,out));
    }
    @Test void cliReportsFeasibleInfeasibleAndInvalidInputSeparately() {
        var buffer = new ByteArrayOutputStream(); var out = new PrintStream(buffer);
        assertEquals(0, Main.run(new String[]{"--input","examples/feasible"},out,out));
        assertTrue(buffer.toString().contains("FEASIBLE"));
        buffer.reset();
        assertEquals(2, Main.run(new String[]{"--input","examples/infeasible"},out,out));
        assertTrue(buffer.toString().contains("INFEASIBLE"));
        buffer.reset();
        assertEquals(1, Main.run(new String[]{"--input","missing-directory"},out,out));
        assertTrue(buffer.toString().contains("Error:"));
        assertEquals(1, Main.run(new String[]{"--bogus"},out,out));
        assertEquals(1, Main.run(new String[]{"--input"},out,out));
        assertEquals(0, Main.run(new String[]{"--help"},out,out));
    }
}
