package com.vonchange.jdbc.abstractjdbc.dialect;

import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;


/**
 *
 * Created by 冯昌义 on 2018/4/16.
 */
public class OracleDialect implements Dialect {
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
