package com.vonchange.jdbc.abstractjdbc.handler;

import com.vonchange.jdbc.abstractjdbc.util.sql.OrmUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2017/12/26.
 */
public class HandlerUtil {
    public static Map<String,Object> rowToMap(ResultSet rs,boolean lower,boolean orm) throws SQLException {
        Map<String,Object> resultMap = new LinkedHashMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }
            if(lower){
                columnName=columnName.toLowerCase();
            }
            if(orm){
                columnName= OrmUtil.toFiled(columnName.toLowerCase());
            }
            resultMap.put(columnName, rs.getObject(col));
        }
        return resultMap;
    }
    public static Map<String,Object> rowToMap(ResultSet rs) throws SQLException{
        return  rowToMap(rs,false,false);
    }

}
