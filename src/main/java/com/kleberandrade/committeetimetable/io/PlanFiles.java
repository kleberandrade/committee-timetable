package com.kleberandrade.committeetimetable.io;

import com.kleberandrade.committeetimetable.domain.Planning.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;

/** Strict UTF-8 TSV boundary. No quoting in TSV input; CSV output follows RFC 4180 escaping. */
public final class PlanFiles {
    private PlanFiles() {}
    public static Request read(Path directory) throws IOException {
        var defenses = new ArrayList<Defense>();
        rows(directory.resolve("defenses.tsv"),"id\ttitle\tminutes\tadvisor\tprofessors\tstudents", row ->
                defenses.add(new Defense(row[0],row[1],Integer.parseInt(row[2]),row[3],ids(row[4]),ids(row[5]))));
        var slots = new ArrayList<Slot>();
        rows(directory.resolve("slots.tsv"),"date\tstart\tend\troom", row -> slots.add(new Slot(window(row,0),row[3])));
        var availability = new HashMap<Person,List<Window>>();
        rows(directory.resolve("availability.tsv"),"role\tid\tdate\tstart\tend", row ->
                availability.computeIfAbsent(new Person(Role.valueOf(row[0]),row[1]),p -> new ArrayList<>()).add(window(row,2)));
        return new Request(defenses,slots,availability);
    }
    private static Set<String> ids(String value) {
        var values = value.split(",",-1);
        var result = new LinkedHashSet<String>();
        for (String id : values) if (!result.add(com.kleberandrade.committeetimetable.domain.Planning.text(id))) throw new IllegalArgumentException("Repeated participant: " + id);
        return result;
    }
    private static Window window(String[] row, int offset) {
        return new Window(LocalDate.parse(row[offset]),LocalTime.parse(row[offset+1]),LocalTime.parse(row[offset+2]));
    }
    private static void rows(Path path, String header, Consumer<String[]> consumer) throws IOException {
        if (Files.size(path) > 1_048_576) throw new IllegalArgumentException(path + ": file exceeds 1 MiB");
        List<String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);
        if (lines.isEmpty() || !header.equals(lines.getFirst())) throw new IllegalArgumentException(path + ":1: expected header " + header);
        int width = header.split("\t").length;
        for (int i = 1; i < lines.size(); i++) {
            try {
                String[] row = lines.get(i).split("\t",-1);
                if (row.length != width) throw new IllegalArgumentException("Expected " + width + " tab-separated columns");
                consumer.accept(row);
            } catch (RuntimeException error) { throw new IllegalArgumentException(path + ":" + (i+1) + ": " + error.getMessage(),error); }
        }
    }
    public static void write(Path path, Result result) throws IOException {
        var lines = new ArrayList<String>();
        lines.add("defense,title,date,start,end,room,professors,students");
        for (Assignment a : result.assignments()) {
            var d = a.defense(); var s = a.slot(); var w = s.window();
            lines.add(String.join(",",List.of(d.id(),d.title(),w.day().toString(),w.start().toString(),w.end().toString(),s.room(),joined(d.professors()),joined(d.students())).stream().map(PlanFiles::csv).toList()));
        }
        Files.write(path,lines,StandardCharsets.UTF_8);
    }
    public static String joined(Set<String> ids) { return String.join(", ",new TreeSet<>(ids)); }
    private static String csv(String value) { return "\"" + value.replace("\"","\"\"") + "\""; }
}
