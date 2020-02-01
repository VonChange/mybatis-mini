package com.vonchange.jdbc.springjdbc.repository;

import com.vonchange.jdbc.abstractjdbc.handler.AbstractMapPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractPageWork;
import com.vonchange.jdbc.abstractjdbc.template.YhJdbcTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2017/12/24.
 */
public interface JdbcRepository {

    <T>  T save(T entity);
    <T>  T saveDuplicateKey(T entity);
    <T> int update(T entity);
    /**
     * 查询对象List
     * @param type 类
     * @param sqlId  markdown里的sqlId
     * @param parameter 占位符参数
     * @param <T> 对象
     * @return  对象List T
     */
    <T> List<T> findListBySqlId(Class<T> type, String sqlId, Map<String, Object> parameter);

    /**
     * 查询对象 onlyOne 默认limit 1
     * @param type 类
     * @param sqlId  markdown里的sqlId
     * @param parameter  占位符参数
     * @param <T> 对象
     * @return 对象 T
     */
    <T> T findBeanBySqlId(Class<T> type, String sqlId, Map<String, Object> parameter);

    /**
     * 查询分页 返回对象
     *  有_count 对应sqlId 取自定义的
     *  @param  type 类
     * @param sqlId mardown 里sqlId  ：sql.sql.findById
     * @param pageable 分页参数
     * @param parameter 占位符参数
     * @return 分页对象
     */
    <T> Page<T>  findPageBySqlId(Class<T> type, String sqlId, Pageable pageable, Map<String, Object> parameter);
    /**
     * 查询列表 返回Map<String,Object>
     * @param sql 原始sql
     * @param parameter  占位符参数
     * @return  List对象 Map<String,Object>
     */
    List<Map<String,Object>> findList(String sql, Map<String, Object> parameter);

    /**
     * 查询一条数据 返回Map 默认limit 1
     * @param sqlId mardown 里sqlId
     * @param parameter 占位符参数
     * @return map
     */
    Map<String,Object> findOneBySqlId(String sqlId, Map<String, Object> parameter);
    /**
     * 查询列表 返回Map<String,Object>
     * @param sqlId markdown里的sqlId
     * @param parameter  占位符参数
     * @return  List对象 Map<String,Object>
     */
    List<Map<String,Object>> findListBySqlId(String sqlId, Map<String, Object> parameter);
    Page<Map<String,Object>>  findPageBySqlIdBigData(String sqlId, AbstractMapPageWork pageWork, Map<String, Object> parameter);

    <T> Page<T>  findPageBySqlIdBigData(Class<T> type, String sqlId, AbstractPageWork pageWork, Map<String, Object> parameter);

    /**
     * 查询分页 返回Map<String,Object>
     *  有_count 对应sqlId 取自定义的
     * @param sqlId mardown 里sqlId  ：sql.sql.findById
     * @param pageable 分页参数
     * @param parameter 占位符参数
     * @return 分页对象 Map<String,Object>
     */
    Page<Map<String,Object>>  findPageBySqlId(String sqlId, Pageable pageable, Map<String, Object> parameter);

    /**
     * 适用于查询max，count 等一个返回值
     * @param sqlId mardown里的sqlId ：sql.sql.findById
     * @param parameter 占位符参数
     * @return 一个值
     */
    Object findBySqlId(String sqlId, Map<String, Object> parameter);

    <T> Map<String, T> queryBySqlId(Class<T> c, String sqlId, String keyInMap, Map<String, Object> parameter);
    int  updateBySqlId(String sqlId, Map<String, Object> parameter);
    Object insertBySqlId(String sqlId, Map<String, Object> parameter);
    long countResult(String sqlId,Map<String, Object> params);
    YhJdbcTemplate getJdbcTemplate(String sql);

}
