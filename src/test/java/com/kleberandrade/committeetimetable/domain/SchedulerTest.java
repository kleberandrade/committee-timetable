package com.kleberandrade.committeetimetable.domain;

import static com.kleberandrade.committeetimetable.domain.Planning.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class SchedulerTest {
  final Window morning = window("09:00", "11:00");

  Window window(String start, String end) {
    return new Window(LocalDate.of(2030, 6, 10), LocalTime.parse(start), LocalTime.parse(end));
  }

  Defense defense(String id, String professor, String student) {
    return new Defense(
        id, "Project " + id, 60, professor, Set.of(professor, "p2", "p3"), Set.of(student));
  }

  Request request(List<Defense> defenses, List<Slot> slots) {
    var availability = new HashMap<Person, List<Window>>();
    for (Defense d : defenses) for (Person p : d.people()) availability.put(p, List.of(morning));
    return new Request(defenses, slots, availability);
  }

  Slot slot(String start, String end, String room) {
    return new Slot(window(start, end), room);
  }

  @Test
  void schedulesAdjacentDefensesWithoutProfessorOverlap() {
    var result =
        new Scheduler()
            .solve(
                request(
                    List.of(defense("a", "p1", "s1"), defense("b", "p1", "s2")),
                    List.of(slot("09:00", "10:00", "R1"), slot("10:00", "11:00", "R1"))),
                1000);
    assertEquals(Status.FEASIBLE, result.status());
    assertEquals(2, result.assignments().size());
    assertFalse(
        result
            .assignments()
            .get(0)
            .slot()
            .window()
            .overlaps(result.assignments().get(1).slot().window()));
  }

  @Test
  void detectsProfessorStudentAndRoomConflicts() {
    for (String resource : List.of("professor", "student", "room")) {
      Defense a = defense("a", "p1", "s1");
      Defense b =
          resource.equals("professor")
              ? defense("b", "p1", "s2")
              : new Defense(
                  "b",
                  "Other",
                  60,
                  "p4",
                  Set.of("p4", "p5", "p6"),
                  Set.of(resource.equals("student") ? "s1" : "s2"));
      var slots =
          resource.equals("room")
              ? List.of(slot("09:00", "10:00", "R1"))
              : List.of(slot("09:00", "10:00", "R1"), slot("09:00", "10:00", "R2"));
      var result = new Scheduler().solve(request(List.of(a, b), slots), 1000);
      assertEquals(Status.INFEASIBLE, result.status(), resource);
      assertTrue(result.assignments().isEmpty());
    }
  }

  @Test
  void fitsDurationAndEveryParticipantsAvailability() {
    var d = defense("a", "p1", "s1");
    assertEquals(
        Status.INFEASIBLE,
        new Scheduler()
            .solve(request(List.of(d), List.of(slot("09:00", "09:59", "R"))), 100)
            .status());
    var r = request(List.of(d), List.of(slot("09:00", "10:00", "R")));
    var availability = new HashMap<>(r.availability());
    availability.put(new Person(Role.STUDENT, "s1"), List.of(window("09:01", "11:00")));
    assertEquals(
        Status.INFEASIBLE,
        new Scheduler().solve(new Request(r.defenses(), r.slots(), availability), 100).status());
  }

  @Test
  void reportsSearchLimitWithoutClaimingInfeasibility() {
    var result =
        new Scheduler()
            .solve(
                request(List.of(defense("a", "p1", "s1")), List.of(slot("09:00", "10:00", "R"))),
                1);
    assertEquals(Status.LIMIT_REACHED, result.status());
    assertTrue(result.assignments().isEmpty());
  }

  @Test
  void rejectsBlankIdentifiersAndNonPositiveBudgets() {
    assertThrows(IllegalArgumentException.class, () -> new Person(Role.PROFESSOR, " "));
    assertThrows(IllegalArgumentException.class, () -> new Person(Role.STUDENT, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Scheduler().solve(request(List.of(), List.of()), 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Scheduler().solve(request(List.of(), List.of()), -1));
  }

  @Test
  void differentDatesNeverConflictAndEndBoundariesMatter() {
    Window next = new Window(morning.day().plusDays(1), morning.start(), morning.end());
    assertFalse(morning.contains(next));
    assertFalse(morning.overlaps(next));
    assertFalse(window("09:00", "10:00").contains(window("09:00", "10:01")));
    assertFalse(window("09:00", "10:00").overlaps(window("10:00", "11:00")));
    assertTrue(morning.contains(window("09:00", "11:00")));
  }

  @Test
  void backtracksWhenFirstCandidatePreventsACompleteSchedule() {
    var a = new Defense("a", "A", 60, "p1", Set.of("p1", "p2", "p3"), Set.of("s1"));
    var b = new Defense("b", "B", 30, "p1", Set.of("p1", "p2", "p3"), Set.of("s2"));
    var r =
        request(
            List.of(a, b),
            List.of(
                slot("09:00", "10:00", "R1"),
                slot("10:00", "11:00", "R1"),
                slot("09:00", "09:30", "R2")));
    var availability = new HashMap<>(r.availability());
    availability.put(new Person(Role.STUDENT, "s2"), List.of(window("09:00", "10:00")));
    var result = new Scheduler().solve(new Request(r.defenses(), r.slots(), availability), 100);
    assertEquals(Status.FEASIBLE, result.status());
    assertEquals(2, result.assignments().size());
    assertTrue(result.visited() > 3);
    assertFalse(Scheduler.conflicts(result.assignments().get(0), result.assignments().get(1)));
  }

  @Test
  void rejectsInstancesBeyondDocumentedMemoryAndRecursionLimits() {
    var defenses =
        java.util.stream.IntStream.range(0, 201)
            .mapToObj(i -> defense("d" + i, "p1", "s1"))
            .toList();
    assertThrows(IllegalArgumentException.class, () -> request(defenses, List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> request(List.of(), Collections.nCopies(5001, slot("09:00", "10:00", "R"))));
    assertEquals(200, request(defenses.subList(0, 200), List.of()).defenses().size());
    assertEquals(
        5000,
        request(List.of(), Collections.nCopies(5000, slot("09:00", "10:00", "R"))).slots().size());
  }

  @Test
  void validatesCompositionAndBoundaries() {
    assertThrows(IllegalArgumentException.class, () -> window("10:00", "09:00"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Defense("a", "A", 0, "p1", Set.of("p1", "p2", "p3"), Set.of("s1")));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Defense("a", "A", 60, "p1", Set.of("p2", "p3", "p4"), Set.of("s1")));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Defense("a", "A", 60, "p1", Set.of("p1", "p2"), Set.of("s1")));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Request(List.of(defense("a", "p1", "s1")), List.of(), Map.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> request(List.of(defense("a", "p1", "s1"), defense("a", "p1", "s1")), List.of()));
    assertEquals(
        Status.FEASIBLE, new Scheduler().solve(request(List.of(), List.of()), 100).status());
  }
}
