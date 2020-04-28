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

import com.vonchange.jsqlparser.parser.ASTNodeAccessImpl;
import com.vonchange.jsqlparser.statement.select.SetOperationList.SetOperationType;

public abstract class SetOperation extends ASTNodeAccessImpl {

    private SetOperationType type;

    public SetOperation(SetOperationType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.name();
    }
}
