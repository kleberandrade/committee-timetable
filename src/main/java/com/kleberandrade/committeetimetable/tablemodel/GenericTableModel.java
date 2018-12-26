/*
 * The MIT License
 *
 * Copyright 2018 Kleber de Oliveira Andrade.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.kleberandrade.committeetimetable.tablemodel;

import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Kleber de Oliveira Andrade
 */
public abstract class GenericTableModel<T> extends AbstractTableModel implements IGenericTableModel<T> {

    protected List<T> list;

    protected String[] columns;

    protected Class<?>[] columnsType;

    public GenericTableModel(List<T> list) {
        this.list = list;
        reload(list);
    }

    @Override
    public int getRowCount() {
        if (list == null) {
            return 0;
        }

        return list.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnsType[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void reload(List<T> t) {
        this.list = t;
        fireTableDataChanged();
    }

    @Override
    public T get(int rowIndex) {
        return list.get(rowIndex);
    }

    @Override
    public void add(T t) {
        list.add(t);
        int lastIndex = getRowCount() - 1;
        fireTableRowsInserted(lastIndex, lastIndex);
    }

    @Override
    public void addList(List<T> t) {
        int index = getRowCount();
        list.addAll(t);
        fireTableRowsInserted(index, index + t.size());
    }

    @Override
    public void update(int rowIndex, T t) {
        list.set(rowIndex, t);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    @Override
    public void delete(int rowIndex) {
        list.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    @Override
    public void clear() {
        list.clear();
        fireTableDataChanged();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
