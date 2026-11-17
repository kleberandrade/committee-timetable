package com.kleberandrade.committeetimetable.adapters;

import static org.junit.jupiter.api.Assertions.*;

import com.kleberandrade.committeetimetable.adapters.in.cli.CommandLine;
import com.kleberandrade.committeetimetable.application.*;
import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.io.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class CommandLineAdapterTest {
  @Test
  void limitReachedUsesExitThreeAndNeverExportsAPartialResult() {
    var generate =
        new GenerateSchedule(
            location -> new Request(List.of(), List.of(), Map.of()),
            (request, limit) -> new Result(Status.LIMIT_REACHED, List.of(), limit),
            1);
    var export =
        new ExportSchedule(
            (location, result, mode) -> {
              throw new AssertionError("Unknown feasibility cannot be exported");
            });
    var buffer = new ByteArrayOutputStream();
    var stream = new PrintStream(buffer);
    assertEquals(
        3,
        new CommandLine(generate, export, stream, stream)
            .run(new String[] {"--input", "in-memory", "--output", "unused.csv"}));
    assertTrue(buffer.toString().contains("LIMIT_REACHED"));
  }
}
