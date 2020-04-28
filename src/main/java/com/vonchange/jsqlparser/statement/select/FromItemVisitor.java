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

public interface FromItemVisitor {

    void visit(Table tableName);

    void visit(SubSelect subSelect);

    void visit(SubJoin subjoin);

    void visit(LateralSubSelect lateralSubSelect);

    void visit(ValuesList valuesList);

    void visit(TableFunction tableFunction);

    public void visit(ParenthesisFromItem aThis);
}
