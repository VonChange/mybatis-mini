package com.vonchange.jdbc.abstractjdbc.util;

import com.vonchange.jdbc.abstractjdbc.model.SqlParmeter;
import com.vonchange.jdbc.abstractjdbc.util.sql.SqlFill;
import com.vonchange.jdbc.abstractjdbc.util.tpl.Tpl;
import com.vonchange.jdbc.abstractjdbc.util.tpl.impl.MybatisTplImpl;

import java.util.Map;

/**
 *
 * Created by 冯昌义 on 2018/5/24.
 */
public class SqlUtil {
    private SqlUtil() {
        throw new IllegalStateException("Utility class");
    }
    public static String getFullSql(String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return SqlFill.fill(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }
    public static  SqlParmeter getSqlParmeter(String sql, Map<String, Object> parameter) {
        Tpl tpl =new MybatisTplImpl();
        return tpl.generate(parameter, sql);
    }
}
