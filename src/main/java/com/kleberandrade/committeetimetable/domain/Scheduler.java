package com.kleberandrade.committeetimetable.domain;

import java.util.*;
import static com.kleberandrade.committeetimetable.domain.Planning.*;

/** Complete backtracking over the supplied candidates, bounded by a node budget. No optimization objective. */
public final class Scheduler {
    public Result solve(Request request, long nodeLimit) {
        if (nodeLimit < 1) throw new IllegalArgumentException("Node limit must be positive");
        var candidates = new LinkedHashMap<Defense,List<Slot>>();
        for (Defense defense : request.defenses()) candidates.put(defense, candidates(request, defense));
        var ordered = new ArrayList<>(request.defenses());
        ordered.sort(Comparator.comparingInt(d -> candidates.get(d).size()));
        return new Search(ordered, candidates, nodeLimit).run();
    }
    private List<Slot> candidates(Request request, Defense defense) {
        return request.slots().stream().filter(s -> s.window().minutes() >= defense.minutes())
                .map(s -> new Slot(new Window(s.window().day(),s.window().start(),s.window().start().plusMinutes(defense.minutes())),s.room()))
                .filter(s -> available(request, defense, s)).distinct().toList();
    }
    private boolean available(Request request, Defense defense, Slot slot) {
        return defense.people().stream().allMatch(p -> request.availability().get(p).stream().anyMatch(w -> w.contains(slot.window())));
    }
    public static boolean conflicts(Assignment a, Assignment b) {
        if (!a.slot().window().overlaps(b.slot().window())) return false;
        return a.slot().room().equals(b.slot().room()) || !Collections.disjoint(a.defense().people(),b.defense().people());
    }
    private static final class Search {
        final List<Defense> ordered;
        final Map<Defense,List<Slot>> candidates;
        final long limit;
        final List<Assignment> selected = new ArrayList<>();
        long visited;
        Search(List<Defense> ordered, Map<Defense,List<Slot>> candidates, long limit) {
            this.ordered = ordered; this.candidates = candidates; this.limit = limit;
        }
        Result run() {
            Status status = visit(0);
            return new Result(status, status == Status.FEASIBLE ? selected : List.of(), visited);
        }
        Status visit(int index) {
            if (Thread.currentThread().isInterrupted() || visited >= limit) return Status.LIMIT_REACHED;
            visited++;
            if (index == ordered.size()) return Status.FEASIBLE;
            Defense defense = ordered.get(index);
            for (Slot slot : candidates.get(defense)) {
                var next = new Assignment(defense,slot);
                if (selected.stream().anyMatch(a -> conflicts(a,next))) continue;
                selected.add(next);
                Status status = visit(index + 1);
                if (status == Status.FEASIBLE) return status;
                selected.removeLast();
                if (status == Status.LIMIT_REACHED) return status;
            }
            return Status.INFEASIBLE;
        }
    }
}
