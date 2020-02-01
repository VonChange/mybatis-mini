package com.vonchange.jdbc.springjdbc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2018/1/9.
 */
public interface JdbcBeanRepository {
      <T> T save(T entity);
    /**
     * 查询对象List
     * @param sqlId  markdown里的sqlId
     * @param parameter 占位符参数
     * @return  对象List T
     */
    <T> List<T> queryListBySqlId(String sqlId, Map<String, Object> parameter);

    /**
     * 查询对象 onlyOne 默认limit 1
     * @param sqlId  markdown里的sqlId
     * @param parameter  占位符参数
     * @return 对象 T
     */
    <T>  T queryBeanBySqlId(String sqlId, Map<String, Object> parameter);

    /**
     * 查询分页 返回对象
     *  有_count 对应sqlId 取自定义的
     * @param sqlId mardown 里sqlId  ：sql.sql.findById
     * @param pageable 分页参数
     * @param parameter 占位符参数
     * @return 分页对象
     */
    <T> Page<T> queryPageBySqlId(String sqlId, Pageable pageable, Map<String, Object> parameter);

}
