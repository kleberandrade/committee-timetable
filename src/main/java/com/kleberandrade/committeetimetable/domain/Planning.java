package com.kleberandrade.committeetimetable.domain;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

/** Immutable scheduling vocabulary. Availability windows and reservations are half-open. */
public final class Planning {
    private Planning() {}
    public static String text(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Identifiers and titles must not be blank");
        return value.strip();
    }
    public enum Role { PROFESSOR, STUDENT }
    public enum Status { FEASIBLE, INFEASIBLE, LIMIT_REACHED }
    public record Person(Role role, String id) {
        public Person { Objects.requireNonNull(role); id = text(id); }
    }
    public record Window(LocalDate day, LocalTime start, LocalTime end) {
        public Window {
            Objects.requireNonNull(day); Objects.requireNonNull(start); Objects.requireNonNull(end);
            if (!start.isBefore(end)) throw new IllegalArgumentException("A window must end after it starts, on the same day");
        }
        public boolean contains(Window other) {
            return day.equals(other.day) && !start.isAfter(other.start) && !end.isBefore(other.end);
        }
        public boolean overlaps(Window other) {
            return day.equals(other.day) && start.isBefore(other.end) && other.start.isBefore(end);
        }
        public long minutes() { return Duration.between(start, end).toMinutes(); }
    }
    public record Slot(Window window, String room) {
        public Slot { Objects.requireNonNull(window); room = text(room); }
    }
    public record Defense(String id, String title, int minutes, String advisor, Set<String> professors, Set<String> students) {
        public Defense {
            id = text(id); title = text(title); advisor = text(advisor);
            professors = identifiers(professors); students = identifiers(students);
            if (minutes < 1 || minutes > 1440) throw new IllegalArgumentException("Duration must be 1–1440 minutes");
            if (professors.size() != 3 || !professors.contains(advisor)) throw new IllegalArgumentException("A committee requires three distinct professors including the advisor");
            if (students.isEmpty()) throw new IllegalArgumentException("At least one student is required");
        }
        public Set<Person> people() {
            var result = new HashSet<Person>();
            professors.forEach(id -> result.add(new Person(Role.PROFESSOR,id)));
            students.forEach(id -> result.add(new Person(Role.STUDENT,id)));
            return Set.copyOf(result);
        }
    }
    private static Set<String> identifiers(Set<String> values) {
        var result = new HashSet<String>();
        for (String value : values) if (!result.add(text(value))) throw new IllegalArgumentException("Duplicate identifier");
        return Set.copyOf(result);
    }
    public record Request(List<Defense> defenses, List<Slot> slots, Map<Person,List<Window>> availability) {
        public Request {
            defenses = List.copyOf(defenses); slots = List.copyOf(slots);
            var copied = new HashMap<Person,List<Window>>();
            availability.forEach((person, windows) -> copied.put(Objects.requireNonNull(person), List.copyOf(windows)));
            availability = Map.copyOf(copied);
            var ids = new HashSet<String>();
            for (Defense d : defenses) {
                if (!ids.add(d.id)) throw new IllegalArgumentException("Duplicate defense: " + d.id);
                for (Person person : d.people()) if (!availability.containsKey(person)) throw new IllegalArgumentException("Missing availability for " + person);
            }
        }
    }
    public record Assignment(Defense defense, Slot slot) {}
    public record Result(Status status, List<Assignment> assignments, long visited) {
        public Result { assignments = List.copyOf(assignments); }
    }
}
