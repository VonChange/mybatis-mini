package com.vonchange.jdbc.abstractjdbc.core;

import java.util.List;
import java.util.Map;

public interface JdbcRepostitory {
    <T> List<T> queryList(Class<T> type, String sqlId, Map<String, Object> parameter);
    int update(String sqlId, Map<String, Object> parameter);
    Object insert(String sqlId, Map<String, Object> parameter);
    <T> T  save(T entity);
    <T> int  update(T entity);
    <T> int  updateAll(T entity);
    <T> T  saveDuplicateKey(T entity);
    <T> T queryOne(Class<T> type, String sqlId, Map<String, Object> parameter);
    <T> T queryById(Class<T> type,Object id);
}

