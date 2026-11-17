package com.kleberandrade.committeetimetable.adapters.out.file;

import com.kleberandrade.committeetimetable.application.PlanningException;
import com.kleberandrade.committeetimetable.application.port.out.ScheduleSource;
import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;

/** Strict UTF-8 TSV input adapter with file/line diagnostics. */
public final class TsvScheduleSource implements ScheduleSource {
  private static final long MAX_FILE_BYTES = 1_048_576;

  @Override
  public Request load(String location) {
    try {
      return read(Path.of(location));
    } catch (IOException failure) {
      throw new PlanningException("Could not read dataset: " + location, failure);
    }
  }

  public static Request read(Path directory) throws IOException {
    var defenses = new ArrayList<Defense>();
    rows(
        directory.resolve("defenses.tsv"),
        "id\ttitle\tminutes\tadvisor\tprofessors\tstudents",
        row ->
            defenses.add(
                new Defense(
                    row[0], row[1], Integer.parseInt(row[2]), row[3], ids(row[4]), ids(row[5]))));
    var slots = new ArrayList<Slot>();
    rows(
        directory.resolve("slots.tsv"),
        "date\tstart\tend\troom",
        row -> slots.add(new Slot(window(row, 0), row[3])));
    var availability = new HashMap<Person, List<Window>>();
    rows(
        directory.resolve("availability.tsv"),
        "role\tid\tdate\tstart\tend",
        row ->
            availability
                .computeIfAbsent(new Person(Role.valueOf(row[0]), row[1]), p -> new ArrayList<>())
                .add(window(row, 2)));
    return new Request(defenses, slots, availability);
  }

  private static Set<String> ids(String value) {
    var values = value.split(",", -1);
    var result = new LinkedHashSet<String>();
    for (String id : values)
      if (!result.add(com.kleberandrade.committeetimetable.domain.Planning.text(id)))
        throw new IllegalArgumentException("Repeated participant: " + id);
    return result;
  }

  private static Window window(String[] row, int offset) {
    return new Window(
        LocalDate.parse(row[offset]),
        LocalTime.parse(row[offset + 1]),
        LocalTime.parse(row[offset + 2]));
  }

  private static void rows(Path path, String header, Consumer<String[]> consumer)
      throws IOException {
    if (Files.size(path) > MAX_FILE_BYTES)
      throw new IllegalArgumentException(path + ": file exceeds 1 MiB");
    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
    if (lines.isEmpty() || !header.equals(lines.getFirst()))
      throw new IllegalArgumentException(path + ":1: expected header " + header);
    int width = header.split("\t").length;
    for (int i = 1; i < lines.size(); i++) {
      try {
        String[] row = lines.get(i).split("\t", -1);
        if (row.length != width)
          throw new IllegalArgumentException("Expected " + width + " tab-separated columns");
        consumer.accept(row);
      } catch (RuntimeException error) {
        throw new IllegalArgumentException(path + ":" + (i + 1) + ": " + error.getMessage(), error);
      }
    }
  }
}
