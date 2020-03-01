package com.vonchange.jdbc.abstractjdbc.core;

import com.vonchange.jdbc.abstractjdbc.handler.AbstractMapPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractPageWork;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IJdbcBase {
    Object insert(String sql, Object[] parameter);

    <T> List<T> queryList(Class<T> type, String sql, Object... args);

    List<Map<String, Object>> queryListResultMap(String sql, Object... args);

    Page<Map<String, Object>> queryForBigData(String sql, AbstractMapPageWork pageWork, Object... args);

    <T> Page<T> queryForBigData(Class<T> type, String sql, AbstractPageWork<T> pageWork, Object... args);

    <T> T queryOne(Class<T> type, String sql, Object... args);

    Map<String, Object> queryUniqueResultMap(String sql, Object... args);

    Object queryOneColumn(String sql, int columnIndex, Object... args);

    <T> Map<String, T> queryMapList(Class<T> c, String sql, String keyInMap, Object... args);

    int update(String sql, Object... args);
}
