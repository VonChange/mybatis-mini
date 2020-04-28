/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.statement;

import com.vonchange.jsqlparser.statement.alter.Alter;
import com.vonchange.jsqlparser.statement.comment.Comment;
import com.vonchange.jsqlparser.statement.create.index.CreateIndex;
import com.vonchange.jsqlparser.statement.create.table.CreateTable;
import com.vonchange.jsqlparser.statement.create.view.AlterView;
import com.vonchange.jsqlparser.statement.create.view.CreateView;
import com.vonchange.jsqlparser.statement.delete.Delete;
import com.vonchange.jsqlparser.statement.drop.Drop;
import com.vonchange.jsqlparser.statement.execute.Execute;
import com.vonchange.jsqlparser.statement.insert.Insert;
import com.vonchange.jsqlparser.statement.merge.Merge;
import com.vonchange.jsqlparser.statement.replace.Replace;
import com.vonchange.jsqlparser.statement.select.Select;
import com.vonchange.jsqlparser.statement.truncate.Truncate;
import com.vonchange.jsqlparser.statement.update.Update;
import com.vonchange.jsqlparser.statement.upsert.Upsert;
import com.vonchange.jsqlparser.statement.values.ValuesStatement;

public interface StatementVisitor {

    void visit(Comment comment);

    void visit(Commit commit);

    void visit(Delete delete);

    void visit(Update update);

    void visit(Insert insert);

    void visit(Replace replace);

    void visit(Drop drop);

    void visit(Truncate truncate);

    void visit(CreateIndex createIndex);

    void visit(CreateTable createTable);

    void visit(CreateView createView);

    void visit(AlterView alterView);

    void visit(Alter alter);

    void visit(Statements stmts);

    void visit(Execute execute);

    void visit(SetStatement set);

    void visit(ShowColumnsStatement set);

    void visit(Merge merge);

    void visit(Select select);

    void visit(Upsert upsert);

    void visit(UseStatement use);

    void visit(Block block);

    void visit(ValuesStatement values);

    void visit(DescribeStatement describe);

    public void visit(ExplainStatement aThis);

    public void visit(ShowStatement aThis);

    public void visit(DeclareStatement aThis);
}
