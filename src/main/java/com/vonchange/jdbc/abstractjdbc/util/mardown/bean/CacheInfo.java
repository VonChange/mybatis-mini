package com.vonchange.jdbc.abstractjdbc.util.mardown.bean;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2017/12/18.
 */
public class CacheInfo {
    private Boolean cache;
    private Long time;
    private Long size;

    public Boolean getCache() {
        return cache;
    }

    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
