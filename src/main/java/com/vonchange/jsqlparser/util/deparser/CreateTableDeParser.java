/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.util.deparser;

import java.util.Iterator;

import com.vonchange.jsqlparser.statement.create.table.ColumnDefinition;
import com.vonchange.jsqlparser.statement.create.table.CreateTable;
import com.vonchange.jsqlparser.statement.create.table.Index;
import com.vonchange.jsqlparser.statement.select.PlainSelect;
import com.vonchange.jsqlparser.statement.select.Select;


public class CreateTableDeParser {

    protected StringBuilder buffer;
    private StatementDeParser statementDeParser;

    public CreateTableDeParser(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public CreateTableDeParser(StatementDeParser statementDeParser, StringBuilder buffer) {
        this.buffer = buffer;
        this.statementDeParser = statementDeParser;
    }


    public void deParse(CreateTable createTable) {
        buffer.append("CREATE ");
        if (createTable.isUnlogged()) {
            buffer.append("UNLOGGED ");
        }
        String params = PlainSelect.
                getStringList(createTable.getCreateOptionsStrings(), false, false);
        if (!"".equals(params)) {
            buffer.append(params).append(' ');
        }

        buffer.append("TABLE ");
        if (createTable.isIfNotExists()) {
            buffer.append("IF NOT EXISTS ");
        }
        buffer.append(createTable.getTable().getFullyQualifiedName());
        if (createTable.getSelect() != null) {
            buffer.append(" AS ");
            if (createTable.isSelectParenthesis()) {
                buffer.append("(");
            }
            Select sel = createTable.getSelect();
            sel.accept(this.statementDeParser);
            if (createTable.isSelectParenthesis()) {
                buffer.append(")");
            }
        } else {
            if (createTable.getColumnDefinitions() != null) {
                buffer.append(" (");
                for (Iterator<ColumnDefinition> iter = createTable.getColumnDefinitions().iterator(); iter.
                        hasNext();) {
                    ColumnDefinition columnDefinition = iter.next();
                    buffer.append(columnDefinition.getColumnName());
                    buffer.append(" ");
                    buffer.append(columnDefinition.getColDataType().toString());
                    if (columnDefinition.getColumnSpecStrings() != null) {
                        for (String s : columnDefinition.getColumnSpecStrings()) {
                            buffer.append(" ");
                            buffer.append(s);
                        }
                    }

                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }

                if (createTable.getIndexes() != null) {
                    for (Iterator<Index> iter = createTable.getIndexes().iterator(); iter.hasNext();) {
                        buffer.append(", ");
                        Index index = iter.next();
                        buffer.append(index.toString());
                    }
                }

                buffer.append(")");
            }
        }

        params = PlainSelect.getStringList(createTable.getTableOptionsStrings(), false, false);
        if (!"".equals(params)) {
            buffer.append(' ').append(params);
        }
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }
}
