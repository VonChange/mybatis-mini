/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.statement.create.table;

import java.util.List;
import com.vonchange.jsqlparser.schema.Table;
import com.vonchange.jsqlparser.statement.select.PlainSelect;

public class ForeignKeyIndex extends NamedConstraint {

    private Table table;
    private List<String> referencedColumnNames;
    private String onDeleteReferenceOption;
    private String onUpdateReferenceOption;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<String> getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public void setReferencedColumnNames(List<String> referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
    }

    public String getOnDeleteReferenceOption() {
        return onDeleteReferenceOption;
    }

    public void setOnDeleteReferenceOption(String onDeleteReferenceOption) {
        this.onDeleteReferenceOption = onDeleteReferenceOption;
    }

    public String getOnUpdateReferenceOption() {
        return onUpdateReferenceOption;
    }

    public void setOnUpdateReferenceOption(String onUpdateReferenceOption) {
        this.onUpdateReferenceOption = onUpdateReferenceOption;
    }

    @Override
    public String toString() {
        String referenceOptions = "";
        if (onDeleteReferenceOption != null) {
            referenceOptions += " ON DELETE " + onDeleteReferenceOption;
        }
        if (onUpdateReferenceOption != null) {
            referenceOptions += " ON UPDATE " + onUpdateReferenceOption;
        }
        return super.toString()
                + " REFERENCES " + table + PlainSelect.
                        getStringList(getReferencedColumnNames(), true, true)
                + referenceOptions;
    }
}
