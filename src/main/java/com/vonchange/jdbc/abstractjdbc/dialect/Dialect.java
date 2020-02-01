package com.vonchange.jdbc.abstractjdbc.dialect;


/**
 *方言接口
 * @author von_change@163.com
 * @date 2015-6-14 下午12:46:50
 */
public interface Dialect {

    /**
     * 获取分页的sql
     * @param sql 要分页sql
     * @param beginNo  开始数
     * @return 分页sql
     */
    String getPageSql(String sql, int beginNo, int pageSize);
    String getLimitOne(String sql);
    int getBigDataFetchSize();
    int getFetchSize();
    String getDialogName();
}
