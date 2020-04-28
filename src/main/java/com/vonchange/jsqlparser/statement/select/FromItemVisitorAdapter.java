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

import com.vonchange.jsqlparser.schema.Table;

public class FromItemVisitorAdapter implements FromItemVisitor {

    @Override
    public void visit(Table table) {

    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(SubJoin subjoin) {

    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(ValuesList valuesList) {

    }

    @Override
    public void visit(TableFunction valuesList) {

    }

    @Override
    public void visit(ParenthesisFromItem aThis) {
        
    }
}
