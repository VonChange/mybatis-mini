/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package com.vonchange.jsqlparser.util;

import java.util.ArrayList;
import java.util.List;
import com.vonchange.jsqlparser.expression.AllComparisonExpression;
import com.vonchange.jsqlparser.expression.AnalyticExpression;
import com.vonchange.jsqlparser.expression.AnyComparisonExpression;
import com.vonchange.jsqlparser.expression.ArrayExpression;
import com.vonchange.jsqlparser.expression.BinaryExpression;
import com.vonchange.jsqlparser.expression.CaseExpression;
import com.vonchange.jsqlparser.expression.CastExpression;
import com.vonchange.jsqlparser.expression.CollateExpression;
import com.vonchange.jsqlparser.expression.DateTimeLiteralExpression;
import com.vonchange.jsqlparser.expression.DateValue;
import com.vonchange.jsqlparser.expression.DoubleValue;
import com.vonchange.jsqlparser.expression.Expression;
import com.vonchange.jsqlparser.expression.ExpressionVisitor;
import com.vonchange.jsqlparser.expression.ExtractExpression;
import com.vonchange.jsqlparser.expression.Function;
import com.vonchange.jsqlparser.expression.HexValue;
import com.vonchange.jsqlparser.expression.IntervalExpression;
import com.vonchange.jsqlparser.expression.JdbcNamedParameter;
import com.vonchange.jsqlparser.expression.JdbcParameter;
import com.vonchange.jsqlparser.expression.JsonExpression;
import com.vonchange.jsqlparser.expression.KeepExpression;
import com.vonchange.jsqlparser.expression.LongValue;
import com.vonchange.jsqlparser.expression.MySQLGroupConcat;
import com.vonchange.jsqlparser.expression.NextValExpression;
import com.vonchange.jsqlparser.expression.NotExpression;
import com.vonchange.jsqlparser.expression.NullValue;
import com.vonchange.jsqlparser.expression.NumericBind;
import com.vonchange.jsqlparser.expression.OracleHierarchicalExpression;
import com.vonchange.jsqlparser.expression.OracleHint;
import com.vonchange.jsqlparser.expression.Parenthesis;
import com.vonchange.jsqlparser.expression.RowConstructor;
import com.vonchange.jsqlparser.expression.SignedExpression;
import com.vonchange.jsqlparser.expression.StringValue;
import com.vonchange.jsqlparser.expression.TimeKeyExpression;
import com.vonchange.jsqlparser.expression.TimeValue;
import com.vonchange.jsqlparser.expression.TimestampValue;
import com.vonchange.jsqlparser.expression.UserVariable;
import com.vonchange.jsqlparser.expression.ValueListExpression;
import com.vonchange.jsqlparser.expression.WhenClause;
import com.vonchange.jsqlparser.expression.operators.arithmetic.*;
import com.vonchange.jsqlparser.expression.operators.conditional.AndExpression;
import com.vonchange.jsqlparser.expression.operators.conditional.OrExpression;
import com.vonchange.jsqlparser.expression.operators.relational.*;
import com.vonchange.jsqlparser.schema.Column;
import com.vonchange.jsqlparser.schema.Table;
import com.vonchange.jsqlparser.statement.Block;
import com.vonchange.jsqlparser.statement.Commit;
import com.vonchange.jsqlparser.statement.DeclareStatement;
import com.vonchange.jsqlparser.statement.DescribeStatement;
import com.vonchange.jsqlparser.statement.ExplainStatement;
import com.vonchange.jsqlparser.statement.SetStatement;
import com.vonchange.jsqlparser.statement.ShowColumnsStatement;
import com.vonchange.jsqlparser.statement.ShowStatement;
import com.vonchange.jsqlparser.statement.Statement;
import com.vonchange.jsqlparser.statement.StatementVisitor;
import com.vonchange.jsqlparser.statement.Statements;
import com.vonchange.jsqlparser.statement.UseStatement;
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
import com.vonchange.jsqlparser.statement.select.AllColumns;
import com.vonchange.jsqlparser.statement.select.AllTableColumns;
import com.vonchange.jsqlparser.statement.select.FromItemVisitor;
import com.vonchange.jsqlparser.statement.select.Join;
import com.vonchange.jsqlparser.statement.select.LateralSubSelect;
import com.vonchange.jsqlparser.statement.select.ParenthesisFromItem;
import com.vonchange.jsqlparser.statement.select.PlainSelect;
import com.vonchange.jsqlparser.statement.select.Select;
import com.vonchange.jsqlparser.statement.select.SelectBody;
import com.vonchange.jsqlparser.statement.select.SelectExpressionItem;
import com.vonchange.jsqlparser.statement.select.SelectItem;
import com.vonchange.jsqlparser.statement.select.SelectItemVisitor;
import com.vonchange.jsqlparser.statement.select.SelectVisitor;
import com.vonchange.jsqlparser.statement.select.SetOperationList;
import com.vonchange.jsqlparser.statement.select.SubJoin;
import com.vonchange.jsqlparser.statement.select.SubSelect;
import com.vonchange.jsqlparser.statement.select.TableFunction;
import com.vonchange.jsqlparser.statement.select.ValuesList;
import com.vonchange.jsqlparser.statement.select.WithItem;
import com.vonchange.jsqlparser.statement.truncate.Truncate;
import com.vonchange.jsqlparser.statement.update.Update;
import com.vonchange.jsqlparser.statement.upsert.Upsert;
import com.vonchange.jsqlparser.statement.values.ValuesStatement;

/**
 * Find all used tables within an select statement.
 *
 * Override extractTableName method to modify the extracted table names (e.g. without schema).
 */
public class TablesNamesFinder implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor, SelectItemVisitor, StatementVisitor {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";
    private List<String> tables;
    private boolean allowColumnProcessing = false;

    private List<String> otherItemNames;

    public List<String> getTableList(Statement statement) {
        init(false);
        statement.accept(this);
        return tables;
    }

    @Override
    public void visit(Select select) {
        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                withItem.accept(this);
            }
        }
        select.getSelectBody().accept(this);
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     */
    public List<String> getTableList(Expression expr) {
        init(true);
        expr.accept(this);
        return tables;
    }

    @Override
    public void visit(WithItem withItem) {
        otherItemNames.add(withItem.getName().toLowerCase());
        withItem.getSelectBody().accept(this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem item : plainSelect.getSelectItems()) {
                item.accept(this);
            }
        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                join.getRightItem().accept(this);
            }
        }
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(this);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(this);
        }
    }

    /**
     * Override to adapt the tableName generation (e.g. with / without schema).
     *
     * @param table
     * @return
     */
    protected String extractTableName(Table table) {
        return table.getFullyQualifiedName();
    }

    @Override
    public void visit(Table tableName) {
        String tableWholeName = extractTableName(tableName);
        if (!otherItemNames.contains(tableWholeName.toLowerCase())
                && !tables.contains(tableWholeName)) {
            tables.add(tableWholeName);
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.getWithItemsList() != null) {
            for (WithItem withItem : subSelect.getWithItemsList()) {
                withItem.accept(this);
            }
        }
        subSelect.getSelectBody().accept(this);
    }

    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(Column tableColumn) {
        if (allowColumnProcessing && tableColumn.getTable() != null && tableColumn.getTable().getName() != null) {
            visit(tableColumn.getTable());
        }
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(IntegerDivision division) {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(DoubleValue doubleValue) {
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(Function function) {
        ExpressionList exprList = function.getParameters();
        if (exprList != null) {
            visit(exprList);
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        if (inExpression.getLeftExpression() != null) {
            inExpression.getLeftExpression().accept(this);
        } else if (inExpression.getLeftItemsList() != null) {
            inExpression.getLeftItemsList().accept(this);
        }
        inExpression.getRightItemsList().accept(this);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.getExpression().accept(this);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue) {
    }

    @Override
    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(NullValue nullValue) {
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    @Override
    public void visit(NotExpression notExpr) {
        notExpr.getExpression().accept(this);
    }

    @Override
    public void visit(BitwiseRightShift expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(BitwiseLeftShift expr) {
        visitBinaryExpression(expr);
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        for (Expression expression : namedExpressionList.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(DateValue dateValue) {
    }

    @Override
    public void visit(TimestampValue timestampValue) {
    }

    @Override
    public void visit(TimeValue timeValue) {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vonchange.jsqlparser.expression.ExpressionVisitor#visit(com.vonchange.jsqlparser.expression.CaseExpression)
     */
    @Override
    public void visit(CaseExpression caseExpression) {
        if (caseExpression.getSwitchExpression() != null) {
            caseExpression.getSwitchExpression().accept(this);
        }
        if (caseExpression.getWhenClauses() != null) {
            for (WhenClause when : caseExpression.getWhenClauses()) {
                when.accept(this);
            }
        }
        if (caseExpression.getElseExpression() != null) {
            caseExpression.getElseExpression().accept(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vonchange.jsqlparser.expression.ExpressionVisitor#visit(com.vonchange.jsqlparser.expression.WhenClause)
     */
    @Override
    public void visit(WhenClause whenClause) {
        if (whenClause.getWhenExpression() != null) {
            whenClause.getWhenExpression().accept(this);
        }
        if (whenClause.getThenExpression() != null) {
            whenClause.getThenExpression().accept(this);
        }
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(SubJoin subjoin) {
        subjoin.getLeft().accept(this);
        for (Join join : subjoin.getJoinList()) {
            join.getRightItem().accept(this);
        }
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat);
    }

    @Override
    public void visit(Matches matches) {
        visitBinaryExpression(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor);
    }

    @Override
    public void visit(CastExpression cast) {
        cast.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo);
    }

    @Override
    public void visit(AnalyticExpression analytic) {
    }

    @Override
    public void visit(SetOperationList list) {
        for (SelectBody plainSelect : list.getSelects()) {
            plainSelect.accept(this);
        }
    }

    @Override
    public void visit(ExtractExpression eexpr) {
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.getSubSelect().getSelectBody().accept(this);
    }
    
    @Override
    public void visit(MultiExpressionList multiExprList) {
        for (ExpressionList exprList : multiExprList.getExprList()) {
            exprList.accept(this);
        }
    }

    @Override
    public void visit(ValuesList valuesList) {
    }

    /**
     * Initializes table names collector. Important is the usage of Column instances to find table
     * names. This is only allowed for expression parsing, where a better place for tablenames could
     * not be there. For complete statements only from items are used to avoid some alias as
     * tablenames.
     *
     * @param allowColumnProcessing
     */
    protected void init(boolean allowColumnProcessing) {
        otherItemNames = new ArrayList<String>();
        tables = new ArrayList<String>();
        this.allowColumnProcessing = allowColumnProcessing;
    }

    @Override
    public void visit(IntervalExpression iexpr) {
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        if (oexpr.getStartExpression() != null) {
            oexpr.getStartExpression().accept(this);
        }

        if (oexpr.getConnectExpression() != null) {
            oexpr.getConnectExpression().accept(this);
        }
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        visitBinaryExpression(rexpr);
    }

    @Override
    public void visit(RegExpMySQLOperator rexpr) {
        visitBinaryExpression(rexpr);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
    }

    @Override
    public void visit(AllColumns allColumns) {
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
    }

    @Override
    public void visit(SelectExpressionItem item) {
        item.getExpression().accept(this);
    }

    @Override
    public void visit(UserVariable var) {
    }

    @Override
    public void visit(NumericBind bind) {

    }

    @Override
    public void visit(KeepExpression aexpr) {
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
    }

    @Override
    public void visit(ValueListExpression valueList) {
        valueList.getExpressionList().accept(this);
    }

    @Override
    public void visit(Delete delete) {
        visit(delete.getTable());

        if (delete.getJoins() != null) {
            for (Join join : delete.getJoins()) {
                join.getRightItem().accept(this);
            }
        }

        if (delete.getWhere() != null) {
            delete.getWhere().accept(this);
        }
    }

    @Override
    public void visit(Update update) {
        visit(update.getTable());
        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                join.getRightItem().accept(this);
            }
        }
        if (update.getExpressions() != null) {
            for (Expression expression : update.getExpressions()) {
                expression.accept(this);
            }
        }

        if (update.getFromItem() != null) {
            update.getFromItem().accept(this);
        }

        if (update.getJoins() != null) {
            for (Join join : update.getJoins()) {
                join.getRightItem().accept(this);
            }
        }

        if (update.getWhere() != null) {
            update.getWhere().accept(this);
        }
    }

    @Override
    public void visit(Insert insert) {
        visit(insert.getTable());
        if (insert.getItemsList() != null) {
            insert.getItemsList().accept(this);
        }
        if (insert.getSelect() != null) {
            visit(insert.getSelect());
        }
    }

    @Override
    public void visit(Replace replace) {
        visit(replace.getTable());
        if (replace.getExpressions() != null) {
            for (Expression expression : replace.getExpressions()) {
                expression.accept(this);
            }
        }
        if (replace.getItemsList() != null) {
            replace.getItemsList().accept(this);
        }
    }

    @Override
    public void visit(Drop drop) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(Truncate truncate) {
        visit(truncate.getTable());
    }

    @Override
    public void visit(CreateIndex createIndex) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(CreateTable create) {
        visit(create.getTable());
        if (create.getSelect() != null) {
            create.getSelect().accept(this);
        }
    }

    @Override
    public void visit(CreateView createView) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(Alter alter) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(Statements stmts) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(Execute execute) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(SetStatement set) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(ShowColumnsStatement set) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        for (Expression expr : rowConstructor.getExprList().getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(HexValue hexValue) {

    }

    @Override
    public void visit(Merge merge) {
        visit(merge.getTable());
        if (merge.getUsingTable() != null) {
            merge.getUsingTable().accept(this);
        } else if (merge.getUsingSelect() != null) {
            merge.getUsingSelect().accept((FromItemVisitor) this);
        }
    }

    @Override
    public void visit(OracleHint hint) {
    }

    @Override
    public void visit(TableFunction valuesList) {
    }

    @Override
    public void visit(AlterView alterView) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {

    }

    @Override
    public void visit(Commit commit) {

    }

    @Override
    public void visit(Upsert upsert) {
        visit(upsert.getTable());
        if (upsert.getItemsList() != null) {
            upsert.getItemsList().accept(this);
        }
        if (upsert.getSelect() != null) {
            visit(upsert.getSelect());
        }
    }

    @Override
    public void visit(UseStatement use) {
    }

    @Override
    public void visit(ParenthesisFromItem parenthesis) {
        parenthesis.getFromItem().accept(this);
    }

    @Override
    public void visit(Block block) {
        if (block.getStatements() != null) {
            visit(block.getStatements());
        }
    }

    @Override
    public void visit(Comment comment) {
        if (comment.getTable() != null) {
            visit(comment.getTable());
        }
        if (comment.getColumn() != null) {
            Table table = comment.getColumn().getTable();
            if (table != null) {
                visit(table);
            }
        }
    }

    @Override
    public void visit(ValuesStatement values) {
        for (Expression expr : values.getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(DescribeStatement describe) {
        describe.getTable().accept(this);
    }

    @Override
    public void visit(ExplainStatement explain) {
        explain.getStatement().accept(this);
    }

    @Override
    public void visit(NextValExpression nextVal) {
    }

    @Override
    public void visit(CollateExpression col) {
        col.getLeftExpression().accept(this);
    }

    @Override
    public void visit(ShowStatement aThis) {
    }

    @Override
    public void visit(SimilarToExpression expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(DeclareStatement aThis) {
    }

    @Override
    public void visit(ArrayExpression array) {
        array.getObjExpression().accept(this);
        array.getIndexExpression().accept(this);
    }
}
