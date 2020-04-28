/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.expression.operators.arithmetic;

import com.vonchange.jsqlparser.expression.BinaryExpression;
import com.vonchange.jsqlparser.expression.ExpressionVisitor;

/**
 * Modulo expression (a % b).
 */
public class Modulo extends BinaryExpression {

    public Modulo() {
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        return "%";
    }
}
