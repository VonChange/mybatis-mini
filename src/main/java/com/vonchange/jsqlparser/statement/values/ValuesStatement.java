/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.statement.values;

import java.util.List;
import com.vonchange.jsqlparser.expression.Expression;
import com.vonchange.jsqlparser.statement.Statement;
import com.vonchange.jsqlparser.statement.StatementVisitor;
import com.vonchange.jsqlparser.statement.select.PlainSelect;
import com.vonchange.jsqlparser.statement.select.SelectBody;
import com.vonchange.jsqlparser.statement.select.SelectVisitor;

public class ValuesStatement implements Statement, SelectBody {
    
    private List<Expression> expressions;
    
    public ValuesStatement(List<Expression> expressions) {
        this.expressions = expressions;
    }
    
    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }
    
    public List<Expression> getExpressions() {
        return expressions;
    }
    
    public void setExpressions(List<Expression> list) {
        expressions = list;
    }
    
    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("VALUES ");
        sql.append(PlainSelect.getStringList(expressions, true, true));
        return sql.toString();
    }
    
    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }
}
