/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.statement.merge;

import java.util.List;
import com.vonchange.jsqlparser.expression.Expression;
import com.vonchange.jsqlparser.schema.Column;
import com.vonchange.jsqlparser.statement.select.PlainSelect;

public class MergeInsert {

    private List<Column> columns = null;
    private List<Expression> values = null;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Expression> getValues() {
        return values;
    }

    public void setValues(List<Expression> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return " WHEN NOT MATCHED THEN INSERT "
                + (columns.isEmpty() ? "" : PlainSelect.getStringList(columns, true, true))
                + " VALUES " + PlainSelect.getStringList(values, true, true);
    }
}
