package com.vonchange.jdbc.abstractjdbc.core;

import com.vonchange.jdbc.abstractjdbc.config.Constants;
import com.vonchange.jdbc.abstractjdbc.handler.*;
import com.vonchange.jdbc.abstractjdbc.model.DataSourceWrapper;
import com.vonchange.jdbc.abstractjdbc.template.MyJdbcTemplate;
import com.vonchange.jdbc.abstractjdbc.util.sql.SqlFill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public abstract class JdbcBaseImpl implements IJdbcBase{
    private static final Logger logger = LoggerFactory.getLogger(JdbcBaseImpl.class);
    protected  abstract MyJdbcTemplate initJdbcTemplate(DataSourceWrapper dataSourceWrapper, Constants.EnumRWType enumRWType, String sql);



    @Override
    public  <T> List<T> queryList(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new BeanListHandler<>(type), args);
    }
    @Override
    public List<Map<String, Object>> queryListResultMap(DataSourceWrapper dataSourceWrapper,String sql, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new MapListHandler(sql), args);
    }

    @Override
    public Page<Map<String, Object>> queryForBigData(DataSourceWrapper dataSourceWrapper,String sql, AbstractMapPageWork pageWork, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.queryBigData(sql, new BigDataMapListHandler(pageWork, sql), args);
    }

    @Override
    public <T> Page<T> queryForBigData(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, AbstractPageWork<T> pageWork, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return (Page<T>) jdbcTemplate.queryBigData(sql, new BigDataBeanListHandler(type, pageWork, sql), args);
    }
    @Override
    public <T> T queryOne(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new BeanHandler<>(type), args);
    }
    @Override
    public Map<String, Object> queryUniqueResultMap(DataSourceWrapper dataSourceWrapper,String sql, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new MapHandler(sql), args);
    }
    @Override
    public   Object queryOneColumn(DataSourceWrapper dataSourceWrapper,String sql, int columnIndex, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new ScalarHandler(columnIndex), args);
    }



    @Override
    public  <T> Map<String, T> queryMapList(DataSourceWrapper dataSourceWrapper,Class<T> c, String sql, String keyInMap, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.read,sql);
        return jdbcTemplate.query(sql, new MapBeanListHandler<>(c, keyInMap), args);
    }

    //write
    @Override
    public Object insert(DataSourceWrapper dataSourceWrapper,String sql, Object[] parameter) {
        logSql(sql, parameter);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.write,sql);
        return jdbcTemplate.insert(sql, new ScalarHandler(), parameter);
    }

    @Override
    public int update(DataSourceWrapper dataSourceWrapper,String sql, Object... args) {
        logSql(sql, args);
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.write,sql);
        return jdbcTemplate.update(sql, args);
    }

    public int[] updateBatch(DataSourceWrapper dataSourceWrapper,String sql, List<Object[]> batchArgs) {
        if(null==batchArgs||batchArgs.isEmpty()){
            return new int[0];
        }
        logSql(sql, batchArgs.get(0));
        MyJdbcTemplate jdbcTemplate = initJdbcTemplate(dataSourceWrapper,Constants.EnumRWType.write,sql);
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void logSql(String sql, Object... params) {
        logger.debug("\n原始sql为:\n{}\n参数为:{}", sql, params);
        logger.debug("生成的sql为:\n{}", SqlFill.fill(sql, params));
    }

}
