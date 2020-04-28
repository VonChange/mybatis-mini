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

public class Top {

    private boolean hasParenthesis = false;
    private boolean isPercentage = false;
    private Expression expression;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public boolean hasParenthesis() {
        return hasParenthesis;
    }

    public void setParenthesis(boolean hasParenthesis) {
        this.hasParenthesis = hasParenthesis;
    }

    public boolean isPercentage() {
        return isPercentage;
    }

    public void setPercentage(boolean percentage) {
        this.isPercentage = percentage;
    }

    @Override
    public String toString() {
        String result = "TOP ";

        if (hasParenthesis) {
            result += "(";
        }

        result += expression.toString();

        if (hasParenthesis) {
            result += ")";
        }

        if (isPercentage) {
            result += " PERCENT";
        }

        return result;
    }
}
