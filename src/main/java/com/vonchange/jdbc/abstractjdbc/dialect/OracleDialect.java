package com.vonchange.jdbc.abstractjdbc.dialect;

import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;


/**
 *
 * Created by 冯昌义 on 2018/4/16.
 */
public class OracleDialect implements Dialect {
 /*   @Override
    public String getPageSql(String sql, int beginNo, int pageSize) {
        if(beginNo==0){
           String  sqlLimit="select row_.*,rownum rn_ from ({0})  row_" +
                    " where rownum <= {1} " ;
            return 	StringUtil.format(sqlLimit, sql, ConvertUtil.toString(beginNo+pageSize));
        }
        String sqlOrg="select * from (select row_.*,rownum rn_ from ({0})  row_" +
                " where rownum <= {1})  where rn_ > {2} " ;
        return 	StringUtil.format(sqlOrg, sql, ConvertUtil.toString(beginNo+pageSize), ConvertUtil.toString(beginNo));
    }*/
    @Override
    public String getPageSql(String sql, int beginNo, int pageSize) {
        if(beginNo==0){
            String  sqlLimit="{0}" +
                    " fetch first {1} rows only" ;
            return 	StringUtils.format(sqlLimit, sql, ConvertUtil.toString(pageSize));
        }
        String sqlOrg="{0}" +
                " offset {1} rows fetch next {2} rows only " ;
        return 	StringUtils.format(sqlOrg, sql, ConvertUtil.toString(beginNo), ConvertUtil.toString(pageSize));
    }

    @Override
    public String getLimitOne(String sql) {
        return sql;
    }

    @Override
    public int getBigDataFetchSize() {
        return 500;
    }

    @Override
    public int getFetchSize() {
        return 500;
    }

    @Override
    public String getDialogName() {
        return ConstantJdbc.Dialog.ORACLE;
    }
}
