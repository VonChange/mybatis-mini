/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.statement.truncate;

import com.vonchange.jsqlparser.schema.Table;
import com.vonchange.jsqlparser.statement.Statement;
import com.vonchange.jsqlparser.statement.StatementVisitor;

public class Truncate implements Statement {

    private Table table;
    boolean cascade;  // to support TRUNCATE TABLE ... CASCADE

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

    public boolean getCascade() {
        return cascade;
    }

    public void setCascade(boolean c) {
        cascade = c;
    }

    @Override
    public String toString() {
        if (cascade) {
            return "TRUNCATE TABLE " + table + " CASCADE";
        }
        return "TRUNCATE TABLE " + table;
    }
}
