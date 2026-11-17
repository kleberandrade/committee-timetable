package com.kleberandrade.committeetimetable.tablemodel;

import static org.junit.jupiter.api.Assertions.*;

import com.kleberandrade.committeetimetable.model.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class TableModelTest {
  @Test
  void ownsItsRowsAndReportsInclusiveInsertionRange() {
    var model = new CourseTableModel(List.of());
    var events = new ArrayList<javax.swing.event.TableModelEvent>();
    model.addTableModelListener(events::add);
    model.addList(List.of(new Course(), new Course()));
    assertEquals(2, model.getRowCount());
    assertEquals(0, events.getFirst().getFirstRow());
    assertEquals(1, events.getFirst().getLastRow());
    model.addList(List.of());
    assertEquals(1, events.size());
    model.clear();
    assertTrue(model.isEmpty());
  }

  @Test
  void missingOptionalRelationshipsRenderEmptyCells() {
    var student = new StudentTableModel(List.of(new Student()));
    assertEquals("", student.getValueAt(0, 4));
    assertEquals("", student.getValueAt(0, 5));
    assertEquals("", new ProfessorTableModel(List.of(new Professor())).getValueAt(0, 3));
    assertEquals(0, new CourseTableModel(null).getRowCount());
  }
}
