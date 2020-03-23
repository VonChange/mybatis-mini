package com.vonchange.jdbc.abstractjdbc.util.tpl.impl;


import com.vonchange.jdbc.abstractjdbc.model.SqlParmeter;
import com.vonchange.jdbc.abstractjdbc.util.tpl.Tpl;
import com.vonchange.mybatis.tpl.MybatisTpl;
import com.vonchange.mybatis.tpl.model.SqlWithParam;

import java.util.Map;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2017/12/12.
 */
public class MybatisTplImpl implements Tpl {
    //Logger logger = LoggerFactory.getLogger(MybatisTplImpl.class);
    @Override
    public SqlParmeter generate(Map<String, Object> data, String tplStr) {
        // 扩展 insert update
        SqlParmeter sqlParmeter= new SqlParmeter();
        SqlWithParam sqlWithParam= MybatisTpl.generate(tplStr,data);
        //logger.debug("use mybatis sql generate::: {}", sqlWithParam.getSql());
        sqlParmeter.setSql(sqlWithParam.getSql());
        sqlParmeter.setParameters(sqlWithParam.getParams());
        sqlParmeter.setPropertyNames(sqlWithParam.getPropertyNames());
        return sqlParmeter;
    }
}
