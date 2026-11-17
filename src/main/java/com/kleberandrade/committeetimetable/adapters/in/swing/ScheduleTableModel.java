package com.kleberandrade.committeetimetable.adapters.in.swing;

import com.kleberandrade.committeetimetable.adapters.shared.ParticipantFormatter;
import com.kleberandrade.committeetimetable.domain.Planning.*;
import com.kleberandrade.committeetimetable.tablemodel.ColumnTableModel;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Read-only presentation of assignments; sorting/formatting is separate from window lifecycle. */
public final class ScheduleTableModel extends ColumnTableModel<Assignment> {
  public ScheduleTableModel() {
    super(
        List.of(),
        List.of(
            new Column<>(
                "Defense / project",
                String.class,
                assignment -> assignment.defense().id() + " · " + assignment.defense().title()),
            new Column<>("Date", LocalDate.class, assignment -> assignment.slot().window().day()),
            new Column<>(
                "Time",
                String.class,
                assignment ->
                    assignment.slot().window().start() + "–" + assignment.slot().window().end()),
            new Column<>("Room", String.class, assignment -> assignment.slot().room()),
            new Column<>(
                "Professors",
                String.class,
                assignment -> ParticipantFormatter.joined(assignment.defense().professors())),
            new Column<>(
                "Students",
                String.class,
                assignment -> ParticipantFormatter.joined(assignment.defense().students()))));
  }

  public void replace(Result result) {
    reload(
        result.assignments().stream()
            .sorted(
                Comparator.comparing((Assignment assignment) -> assignment.slot().window().day())
                    .thenComparing(assignment -> assignment.slot().window().start())
                    .thenComparing(assignment -> assignment.defense().id()))
            .toList());
  }
}
