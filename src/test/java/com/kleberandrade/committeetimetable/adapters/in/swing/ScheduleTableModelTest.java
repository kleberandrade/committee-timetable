package com.kleberandrade.committeetimetable.adapters.in.swing;

import static org.junit.jupiter.api.Assertions.*;

import com.kleberandrade.committeetimetable.domain.Scheduler;
import com.kleberandrade.committeetimetable.io.PlanFiles;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ScheduleTableModelTest {
  @Test
  void displaysChronologicalReadOnlyRowsAndCanReset() throws Exception {
    var result = new Scheduler().solve(PlanFiles.read(Path.of("examples/feasible")), 100_000);
    var model = new ScheduleTableModel();
    model.replace(result);
    assertEquals(3, model.getRowCount());
    assertEquals("D01 · Water, safely", model.getValueAt(0, 0));
    assertEquals("09:00–10:00", model.getValueAt(0, 2));
    assertEquals("10:00–11:00", model.getValueAt(2, 2));
    assertEquals("P01, P02, P03", model.getValueAt(0, 4));
    assertEquals(LocalDate.class, model.getColumnClass(1));
    assertFalse(model.isCellEditable(0, 0));
    model.clear();
    assertEquals(0, model.getRowCount());
  }
}
