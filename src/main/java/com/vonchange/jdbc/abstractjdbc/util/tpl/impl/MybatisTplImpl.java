package com.vonchange.jdbc.abstractjdbc.util.tpl.impl;


import com.vonchange.jdbc.abstractjdbc.model.SqlParmeter;
import com.vonchange.jdbc.abstractjdbc.util.tpl.Tpl;
import com.vonchange.mybatis.tpl.MybatisTpl;
import com.vonchange.mybatis.tpl.model.SqlWithParam;

import java.util.Map;

/**
 * @author 冯昌义
 *  2017/12/12.
 */
public class MybatisTplImpl implements Tpl {
    @Override
    public SqlParmeter generate(Map<String, Object> data, String tplStr) {
        // 扩展 insert update
        SqlParmeter sqlParmeter= new SqlParmeter();
        SqlWithParam sqlWithParam= MybatisTpl.generate(tplStr,data);
        sqlParmeter.setSql(sqlWithParam.getSql());
        sqlParmeter.setParameters(sqlWithParam.getParams());
        sqlParmeter.setPropertyNames(sqlWithParam.getPropertyNames());
        return sqlParmeter;
    }
}
