/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.statement.comment;

import com.vonchange.jsqlparser.expression.StringValue;
import com.vonchange.jsqlparser.schema.Column;
import com.vonchange.jsqlparser.schema.Table;
import com.vonchange.jsqlparser.statement.Statement;
import com.vonchange.jsqlparser.statement.StatementVisitor;

public class Comment implements Statement {

    private Table table;
    private Column column;
    private StringValue comment;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public StringValue getComment() {
        return comment;
    }

    public void setComment(StringValue comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        String sql = "COMMENT ON ";
        if (table != null) {
            sql += "TABLE " + table + " ";
        } else if (column != null) {
            sql += "COLUMN " + column + " ";
        }
        sql += "IS " + comment;
        return sql;
    }
}
