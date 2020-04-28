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

import com.vonchange.jsqlparser.expression.Alias;

public interface FromItem {

    void accept(FromItemVisitor fromItemVisitor);

    Alias getAlias();

    void setAlias(Alias alias);

    Pivot getPivot();

    void setPivot(Pivot pivot);

    UnPivot getUnPivot();

    void setUnPivot(UnPivot unpivot);

}
