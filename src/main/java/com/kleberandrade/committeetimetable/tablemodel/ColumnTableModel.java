package com.kleberandrade.committeetimetable.tablemodel;

import java.util.List;
import java.util.function.Function;

/** Shared column definitions keep the public entity-specific table models small. */
public class ColumnTableModel<T> extends GenericTableModel<T> {
  public record Column<T>(String title, Class<?> type, Function<T, Object> read) {}

  private final List<Column<T>> definitions;

  public ColumnTableModel(List<T> rows, List<Column<T>> definitions) {
    super(rows);
    this.definitions = List.copyOf(definitions);
    this.columns = definitions.stream().map(Column::title).toArray(String[]::new);
    this.columnsType = definitions.stream().map(Column::type).toArray(Class<?>[]::new);
  }

  @Override
  public Object getValueAt(int row, int column) {
    return definitions.get(column).read().apply(get(row));
  }
}
