package com.vonchange.jdbc.abstractjdbc.core;

import com.vonchange.jdbc.abstractjdbc.handler.AbstractMapPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractPageWork;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
//Repository
public interface JdbcRepository {
    <T> List<T> queryList(Class<T> type, String sqlId, Map<String, Object> parameter);
    int update(String sqlId, Map<String, Object> parameter);
    Object insert(String sqlId, Map<String, Object> parameter);
    <T> T  save(T entity);
    <T> int  update(T entity);
    <T> int  updateAll(T entity);
    <T> T  saveDuplicateKey(T entity);
    <T> T queryOne(Class<T> type, String sqlId, Map<String, Object> parameter);
    <T> T queryById(Class<T> type,Object id);
    <T> Page<T> queryPage(Class<T> type, String sqlId, Pageable pageable, Map<String, Object> parameter);
    <T> T queryOneColumn(Class<?> targetType,String sqlId, Map<String, Object> parameter);
    <T> Page<T> queryBigData(Class<T> type, String sqlId, AbstractPageWork pageWork, Map<String, Object> parameter);
    // map
    Page<Map<String, Object>> queryBigData(String sqlId, AbstractMapPageWork pageWork, Map<String, Object> parameter);
    Map<String, Object> queryOne(String sqlId, Map<String, Object> parameter);
    Page<Map<String, Object>> queryPage(String sqlId, Pageable pageable, Map<String, Object> parameter);
    List<Map<String, Object>> queryList(String sqlId, Map<String, Object> parameter);
}

