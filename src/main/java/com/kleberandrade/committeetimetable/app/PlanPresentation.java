package com.kleberandrade.committeetimetable.app;
import com.kleberandrade.committeetimetable.domain.Planning.*;

/** Text shared by desktop states, independently testable without a display server. */
public final class PlanPresentation {
    private PlanPresentation() {}
    public static String title(Status status) {
        return switch (status) { case FEASIBLE -> "Feasible"; case INFEASIBLE -> "Infeasible"; case LIMIT_REACHED -> "Search limit"; };
    }
    public static String message(Result plan) {
        return explanation(plan) + " Search nodes: " + plan.visited() + ".";
    }
    private static String explanation(Result plan) {
        return switch (plan.status()) {
            case FEASIBLE -> feasible(plan);
            case INFEASIBLE -> "No complete schedule exists among these candidate slots. Review room capacity, duration and each participant’s availability.";
            case LIMIT_REACHED -> "Search stopped at its node limit. Feasibility remains unknown. Reduce the dataset or refine candidate slots.";
        };
    }
    private static String feasible(Result plan) {
        return plan.assignments().isEmpty() ? "The input contains no defenses. Add defenses to generate a timetable." : "All defenses scheduled with no supported conflicts. Export CSV to review or share. This is a feasible plan, not a global optimum.";
    }
}
