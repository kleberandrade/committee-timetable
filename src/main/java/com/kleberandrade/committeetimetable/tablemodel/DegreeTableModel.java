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

import com.kleberandrade.committeetimetable.model.Degree;
import java.util.List;

/**
 *
 * @author Kleber de Oliveira Andrade
 */
public class DegreeTableModel extends GenericTableModel<Degree> {

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    
    public DegreeTableModel(List<Degree> degrees) {
        super(degrees);
        this.columns = new String[]{"Id", "Nome"};
        this.columnsType = new Class<?>[]{Long.class, String.class};
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Degree degree = get(rowIndex);
        switch (columnIndex) {
            case COL_ID:
                return degree.getId();
            case COL_NAME:
                return degree.getName();
            default:
                break;
        }
        return null;
    }
}
