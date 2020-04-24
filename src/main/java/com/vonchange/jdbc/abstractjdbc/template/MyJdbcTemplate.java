package com.vonchange.jdbc.abstractjdbc.template;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.*;

public class MyJdbcTemplate extends JdbcTemplate {
    public MyJdbcTemplate() {
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public MyJdbcTemplate(DataSource dataSource) {
       super(dataSource);
    }
    protected <T> T insert(final PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) throws DataAccessException {

        logger.debug("Executing prepared SQL update");
        return execute(psc, new PreparedStatementCallback<T>() {

            public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
                T generatedKeys = null;
                try {
                    if (pss != null) {
                        pss.setValues(ps);
                    }
                    ps.executeUpdate();
                    ResultSet resultSet = ps.getGeneratedKeys();
                    generatedKeys = rse.extractData(resultSet);
                    if (logger.isDebugEnabled()) {
                        logger.debug("generatedKeys : " + generatedKeys);
                    }
                    return generatedKeys;
                } finally {
                    if (pss instanceof ParameterDisposer) {
                        ((ParameterDisposer) pss).cleanupParameters();
                    }
                }
            }
        });
    }

    private <T> T insert(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
        return insert(new InsertPreparedStatementCreator(sql), pss, rse);
    }


    public <T> T insert(String sql, ResultSetExtractor<T> rse, Object... params) {
        return insert(sql, newArgPreparedStatementSetter(params), rse);
    }
    private static class InsertPreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

        private final String sql;

        public InsertPreparedStatementCreator(String sql) {
            Assert.notNull(sql, "SQL must not be null");
            this.sql = sql;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			/*//方言
			String dialog = SqlCommentUtil.getDialect(this.sql);
			if(dialog.equals(SqlCommentUtil.Dialect.ORACLE)){
				//  暂时写死  有变动 从sql注释里  定义 获取
				//,  new String[]{"ID"} ,  new String[]{"id","code_no"}
				return con.prepareStatement(this.sql, new String[]{"ID"});
			}*/
            return con.prepareStatement(this.sql,  Statement.RETURN_GENERATED_KEYS);
        }
        public String getSql() {
            return this.sql;
        }
    }

    private int fetchSizeBigData=0;

    public int getFetchSizeBigData() {
        return fetchSizeBigData;
    }

    public void setFetchSizeBigData(int fetchSizeBigData) {
        this.fetchSizeBigData = fetchSizeBigData;
    }

    public <T> T  queryBigData(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException {
        return queryBigData(sql, newArgPreparedStatementSetter(args), rse);
    }
    public <T> T queryBigData(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
        int fetchSize = getFetchSizeBigData();
        return query(new BigDataPreparedStatementCreator(sql,fetchSize), pss, rse);
    }


    private static class BigDataPreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

        private final String sql;
        private final  int fetchSize;

        public BigDataPreparedStatementCreator(String sql,int fetchSize) {
            Assert.notNull(sql, "SQL must not be null");
            this.sql = sql;
            this.fetchSize=fetchSize;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(this.fetchSize);
            ps.setFetchDirection(ResultSet.FETCH_REVERSE);
            return ps;
        }

        @Override
        public String getSql() {
            return this.sql;
        }
    }
}
