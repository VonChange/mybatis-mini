package com.vonchange.jdbc.abstractjdbc.core;

import com.vonchange.jdbc.abstractjdbc.config.Constants;
import com.vonchange.jdbc.abstractjdbc.handler.*;
import com.vonchange.jdbc.abstractjdbc.template.YhJdbcTemplate;
import com.vonchange.jdbc.abstractjdbc.util.sql.SqlFill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public abstract class JdbcBaseImpl implements IJdbcBase{
    private static final Logger logger = LoggerFactory.getLogger(JdbcBaseImpl.class);
    protected  abstract YhJdbcTemplate initJdbcTemplate(String sql,Constants.EnumRWType enumRWType);
    @Override
    public Object insert(String sql, Object[] parameter) {
        logSql(sql, parameter);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.write);
        return jdbcTemplate.insert(sql, new ScalarHandler(), parameter);
    }

    @Override
    public  <T> List<T> queryList(Class<T> type, String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.read);
        return jdbcTemplate.query(sql, new BeanListHandler<>(type), args);
    }
    @Override
    public List<Map<String, Object>> queryListResultMap(String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.read);
        return jdbcTemplate.query(sql, new MapListHandler(sql), args);
    }

    @Override
    public Page<Map<String, Object>> queryForBigData(String sql, AbstractMapPageWork pageWork, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.read);
        return jdbcTemplate.queryBigData(sql, new BigDataMapListHandler(pageWork, sql), args);
    }

    @Override
    public <T> Page<T> queryForBigData(Class<T> type, String sql, AbstractPageWork<T> pageWork, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.read);
        return (Page<T>) jdbcTemplate.queryBigData(sql, new BigDataBeanListHandler(type, pageWork, sql), args);
    }
    @Override
    public <T> T queryOne(Class<T> type, String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.read);
        return jdbcTemplate.query(sql, new BeanHandler<>(type), args);
    }
    @Override
    public Map<String, Object> queryUniqueResultMap(String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.read);
        return jdbcTemplate.query(sql, new MapHandler(sql), args);
    }
    @Override
    public   Object queryOneColumn(String sql, int columnIndex, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.read);
        return jdbcTemplate.query(sql, new ScalarHandler(columnIndex), args);
    }


    private void logSql(String sql, Object... params) {
        logger.debug("\n原始sql为:\n{}\n参数为:{}", sql, params);
        logger.debug("生成的sql为:\n{}", SqlFill.fill(sql, params));
    }

    @Override
    public  <T> Map<String, T> queryMapList(Class<T> c, String sql, String keyInMap, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.read);
        return jdbcTemplate.query(sql, new MapBeanListHandler<>(c, keyInMap), args);
    }
    @Override
    public int update(String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = initJdbcTemplate(sql,Constants.EnumRWType.write);
        return jdbcTemplate.update(sql, args);
    }

}
