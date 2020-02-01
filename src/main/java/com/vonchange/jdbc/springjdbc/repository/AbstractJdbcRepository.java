package com.vonchange.jdbc.springjdbc.repository;

import com.vonchange.jdbc.abstractjdbc.core.AbstractJbdcCore;
import com.vonchange.jdbc.abstractjdbc.handler.*;
import com.vonchange.jdbc.abstractjdbc.template.YhJdbcTemplate;
import com.vonchange.jdbc.abstractjdbc.util.sql.SqlFill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author 冯昌义
 *         2017/12/24.
 */

public abstract class AbstractJdbcRepository extends AbstractJbdcCore {
    private static final Logger logger = LoggerFactory.getLogger(AbstractJdbcRepository.class);



    @Override
    protected Object insert(String sql, Object[] parameter) {
        logSql(sql, parameter);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        Object object = jdbcTemplate.insert(sql, new ScalarHandler(), parameter);
        return object;
    }

    @Override
    protected <T> List<T> queryList(Class<T> type, String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        List<T> result = jdbcTemplate.query(sql, new BeanListHandler<>(type), args);
        return result;
    }

    @Override
    protected List<Map<String, Object>> queryListResultMap(String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        return jdbcTemplate.query(sql, new MapListHandler(sql), args);
    }

    @Override
    protected Page<Map<String, Object>> queryForBigData(String sql, AbstractMapPageWork pageWork, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        return jdbcTemplate.queryBigData(sql, new BigDataMapListHandler(pageWork, sql), args);
    }

    @Override
    protected <T> Page<T> queryForBigData(Class<T> type, String sql, AbstractPageWork<T> pageWork, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        return (Page<T>) jdbcTemplate.queryBigData(sql, new BigDataBeanListHandler(type, pageWork, sql), args);
    }

    @Override
    protected <T> T queryOne(Class<T> type, String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        return jdbcTemplate.query(sql, new BeanHandler<>(type), args);
    }

    @Override
    protected Map<String, Object> queryUniqueResultMap(String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        return jdbcTemplate.query(sql, new MapHandler(sql), args);
    }

    @Override
    protected Object queryOneColumn(String sql, int columnIndex, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        return jdbcTemplate.query(sql, new ScalarHandler(columnIndex), args);
    }


    private void logSql(String sql, Object... params) {
        logger.debug("\n原始sql为:\n{}\n参数为:{}\n", sql, params);
        logger.debug("生成的sql为:\n{}", SqlFill.fill(sql, params));
    }


    protected <T> Map<String, T> queryMapList(Class<T> c, String sql, String keyInMap, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        return jdbcTemplate.query(sql, new MapBeanListHandler<>(c, keyInMap), args);
    }

    protected int update(String sql, Object... args) {
        logSql(sql, args);
        YhJdbcTemplate jdbcTemplate = getJdbcTemplate(sql);
        return jdbcTemplate.update(sql, args);
    }



}
