package com.vonchange.jdbc.abstractjdbc.core;

import com.vonchange.jdbc.abstractjdbc.config.Constants;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractMapPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.BeanHandler;
import com.vonchange.jdbc.abstractjdbc.handler.BeanInsertHandler;
import com.vonchange.jdbc.abstractjdbc.handler.BeanListHandler;
import com.vonchange.jdbc.abstractjdbc.handler.BigDataBeanListHandler;
import com.vonchange.jdbc.abstractjdbc.handler.BigDataMapListHandler;
import com.vonchange.jdbc.abstractjdbc.handler.MapBeanListHandler;
import com.vonchange.jdbc.abstractjdbc.handler.MapHandler;
import com.vonchange.jdbc.abstractjdbc.handler.MapListHandler;
import com.vonchange.jdbc.abstractjdbc.handler.ScalarHandler;
import com.vonchange.jdbc.abstractjdbc.model.DataSourceWrapper;
import com.vonchange.jdbc.abstractjdbc.template.MyJdbcTemplate;
import com.vonchange.jdbc.abstractjdbc.util.sql.SqlFill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class JdbcBaseImpl implements IJdbcBase{
    private static final Logger log = LoggerFactory.getLogger(JdbcBaseImpl.class);
    protected  abstract MyJdbcTemplate initJdbcTemplate(DataSourceWrapper dataSourceWrapper, Constants.EnumRWType enumRWType, String sql);

    protected abstract boolean logReadSwitch();
    protected abstract boolean logWriteSwitch();
    protected abstract boolean logFullSqlSwitch();

    @Override
    public  <T> List<T> queryList(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, Object... args) {
        logSql(Constants.EnumRWType.read,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new BeanListHandler<>(type), args);
    }
    @Override
    public List<Map<String, Object>> queryListResultMap(DataSourceWrapper dataSourceWrapper,String sql, Object... args) {
        logSql(Constants.EnumRWType.read,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new MapListHandler(sql), args);
    }

    @Override
    public void queryForBigData(DataSourceWrapper dataSourceWrapper,String sql, AbstractMapPageWork pageWork, Object... args) {
        logSql(Constants.EnumRWType.read,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        jdbcTemplate.queryBigData(sql, new BigDataMapListHandler(pageWork, sql), args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void queryForBigData(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, AbstractPageWork<T> pageWork, Object... args) {
        logSql(Constants.EnumRWType.read,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        jdbcTemplate.queryBigData(sql, new BigDataBeanListHandler(type, pageWork, sql), args);
    }
    @Override
    public <T> T queryOne(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, Object... args) {
        logSql(Constants.EnumRWType.read,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new BeanHandler<>(type), args);
    }
    @Override
    public Map<String, Object> queryUniqueResultMap(DataSourceWrapper dataSourceWrapper,String sql, Object... args) {
        logSql(Constants.EnumRWType.read,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new MapHandler(sql), args);
    }
    @Override
    public   Object queryOneColumn(DataSourceWrapper dataSourceWrapper,String sql, int columnIndex, Object... args) {
        logSql(Constants.EnumRWType.read,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new ScalarHandler(columnIndex), args);
    }



    @Override
    public  <T> Map<String, T> queryMapList(DataSourceWrapper dataSourceWrapper,Class<T> c, String sql, String keyInMap, Object... args) {
        logSql(Constants.EnumRWType.read,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new MapBeanListHandler<>(c, keyInMap), args);
    }

    //write
    @Override
    public <T> int insert(DataSourceWrapper dataSourceWrapper,T entity,String sql,List<String> columnReturn, Object[] parameter) {
        logSql(Constants.EnumRWType.write,sql, parameter);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.write,sql);
        return jdbcTemplate.insert(sql,columnReturn, new BeanInsertHandler<>(entity), parameter);
    }
    @Override
    public  int insert(DataSourceWrapper dataSourceWrapper,String sql,Object[] parameter) {
        logSql(Constants.EnumRWType.write,sql, parameter);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.write,sql);
        return jdbcTemplate.insert(sql,null, new ScalarHandler(), parameter);
    }

    @Override
    public int update(DataSourceWrapper dataSourceWrapper,String sql, Object... args) {
        logSql(Constants.EnumRWType.write,sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.write,sql);
        return jdbcTemplate.update(sql, args);
    }

    public int[] updateBatch(DataSourceWrapper dataSourceWrapper,String sql, List<Object[]> batchArgs) {
        if(null==batchArgs||batchArgs.isEmpty()){
            return new int[0];
        }
        logSql(Constants.EnumRWType.write,sql, batchArgs.get(0));
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.write,sql);
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void logSql(Constants.EnumRWType enumRWType,String sql, Object... params) {
        if(log.isDebugEnabled()){
            log.debug("\norg sql: {}\nparams: {}", sql, params);
            String sqlResult=SqlFill.fill(sql, params);
            log.debug("\nresult sql: {}", sqlResult);
        }
        if(log.isInfoEnabled()){
            if(enumRWType.equals(Constants.EnumRWType.write)&&logWriteSwitch()){
                log.info("\nwrite org sql: {}\n参数为:{}", sql, params);
                if(logFullSqlSwitch()){
                    String sqlResult=SqlFill.fill(sql, params);
                    log.info("\nwrite result sql: {}", sqlResult);
                }
            }
            if(enumRWType.equals(Constants.EnumRWType.read)&&logReadSwitch()){
                log.info("\nread org sql: {}\n参数为:{}", sql, params);
                if(logFullSqlSwitch()){
                    String sqlResult=SqlFill.fill(sql, params);
                    log.info("\nread result sql: {}", sqlResult);
                }
            }
        }
    }

}
