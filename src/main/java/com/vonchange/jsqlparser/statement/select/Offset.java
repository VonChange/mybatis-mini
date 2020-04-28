/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.statement.select;

import com.vonchange.jsqlparser.expression.Expression;
import com.vonchange.jsqlparser.expression.JdbcNamedParameter;
import com.vonchange.jsqlparser.expression.JdbcParameter;

public class Offset {

    private long offset;
    private Expression offsetJdbcParameter = null;
    private String offsetParam = null;

    public long getOffset() {
        return offset;
    }

    public String getOffsetParam() {
        return offsetParam;
    }

    public void setOffset(long l) {
        offset = l;
    }

    public void setOffsetParam(String s) {
        offsetParam = s;
    }

    public Expression getOffsetJdbcParameter() {
        return offsetJdbcParameter;
    }

    public void setOffsetJdbcParameter(JdbcParameter jdbc) {
        offsetJdbcParameter = jdbc;
    }
    
    public void setOffsetJdbcParameter(JdbcNamedParameter jdbc) {
        offsetJdbcParameter = jdbc;
    }

    @Override
    public String toString() {
        return " OFFSET " + (offsetJdbcParameter!=null ? offsetJdbcParameter.toString() : offset) + (offsetParam != null ? " " + offsetParam : "");
    }
}
