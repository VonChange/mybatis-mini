package com.vonchange.jdbc.abstractjdbc.core;

import com.vonchange.jdbc.abstractjdbc.handler.AbstractMapPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractPageWork;
import com.vonchange.jdbc.abstractjdbc.model.DataSourceWrapper;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IJdbcBase {
    Object insert(DataSourceWrapper dataSourceWrapper, String sql, Object[] parameter);

    <T> List<T> queryList(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, Object... args);

    List<Map<String, Object>> queryListResultMap(DataSourceWrapper dataSourceWrapper,String sql, Object... args);

    Page<Map<String, Object>> queryForBigData(DataSourceWrapper dataSourceWrapper,String sql, AbstractMapPageWork pageWork, Object... args);

    <T> Page<T> queryForBigData(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, AbstractPageWork<T> pageWork, Object... args);

    <T> T queryOne(DataSourceWrapper dataSourceWrapper,Class<T> type, String sql, Object... args);

    Map<String, Object> queryUniqueResultMap(DataSourceWrapper dataSourceWrapper,String sql, Object... args);

    Object queryOneColumn(DataSourceWrapper dataSourceWrapper,String sql, int columnIndex, Object... args);

    <T> Map<String, T> queryMapList(DataSourceWrapper dataSourceWrapper,Class<T> c, String sql, String keyInMap, Object... args);

    int update(DataSourceWrapper dataSourceWrapper,String sql, Object... args);
    int[] updateBatch(DataSourceWrapper dataSourceWrapper,String sql, List<Object[]> batchArgs);
}
