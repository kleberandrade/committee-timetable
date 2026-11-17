package com.kleberandrade.committeetimetable.domain;

import static com.kleberandrade.committeetimetable.domain.Planning.*;

import java.util.*;

/**
 * Complete backtracking over the supplied candidates, bounded by a node budget. No optimization
 * objective.
 */
public final class Scheduler implements ScheduleSolver {
  @Override
  public Result solve(Request request, long nodeLimit) {
    if (nodeLimit < 1) throw new IllegalArgumentException("Node limit must be positive");
    var candidates = new LinkedHashMap<Defense, List<Slot>>();
    for (Defense defense : request.defenses())
      candidates.put(defense, candidates(request, defense));
    var ordered = new ArrayList<>(request.defenses());
    ordered.sort(Comparator.comparingInt(defense -> candidates.get(defense).size()));
    return new Search(ordered, candidates, nodeLimit).run();
  }

  private List<Slot> candidates(Request request, Defense defense) {
    return request.slots().stream()
        .filter(slot -> slot.window().minutes() >= defense.minutes())
        .map(
            slot ->
                new Slot(
                    new Window(
                        slot.window().day(),
                        slot.window().start(),
                        slot.window().start().plusMinutes(defense.minutes())),
                    slot.room()))
        .filter(slot -> available(request, defense, slot))
        .distinct()
        .toList();
  }

  private boolean available(Request request, Defense defense, Slot slot) {
    return defense.people().stream()
        .allMatch(
            person ->
                request.availability().get(person).stream()
                    .anyMatch(window -> window.contains(slot.window())));
  }

  public static boolean conflicts(Assignment first, Assignment second) {
    if (!first.slot().window().overlaps(second.slot().window())) return false;
    return first.slot().room().equals(second.slot().room())
        || !Collections.disjoint(first.defense().people(), second.defense().people());
  }

  private static final class Search {
    final List<Defense> orderedDefenses;
    final Map<Defense, List<Slot>> candidateSlots;
    final long nodeLimit;
    final List<Assignment> selectedAssignments = new ArrayList<>();
    long visitedNodes;

    Search(List<Defense> orderedDefenses, Map<Defense, List<Slot>> candidateSlots, long nodeLimit) {
      this.orderedDefenses = orderedDefenses;
      this.candidateSlots = candidateSlots;
      this.nodeLimit = nodeLimit;
    }

    Result run() {
      Status status = searchFrom(0);
      return new Result(
          status, status == Status.FEASIBLE ? selectedAssignments : List.of(), visitedNodes);
    }

    Status searchFrom(int defenseIndex) {
      if (Thread.currentThread().isInterrupted() || visitedNodes >= nodeLimit)
        return Status.LIMIT_REACHED;
      visitedNodes++;
      if (defenseIndex == orderedDefenses.size()) return Status.FEASIBLE;
      Defense defense = orderedDefenses.get(defenseIndex);
      for (Slot slot : candidateSlots.get(defense)) {
        var next = new Assignment(defense, slot);
        if (selectedAssignments.stream().anyMatch(a -> conflicts(a, next))) continue;
        selectedAssignments.add(next);
        Status status = searchFrom(defenseIndex + 1);
        if (status == Status.FEASIBLE) return status;
        selectedAssignments.removeLast();
        if (status == Status.LIMIT_REACHED) return status;
      }
      return Status.INFEASIBLE;
    }
  }
}
