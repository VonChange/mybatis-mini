package com.vonchange.jdbc.abstractjdbc.util.cache;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2017/12/18.
 */
public interface ICache<V> {
    V get(String key);
     void put(String key, V value);

    void put(String key, V value, long time);
}
