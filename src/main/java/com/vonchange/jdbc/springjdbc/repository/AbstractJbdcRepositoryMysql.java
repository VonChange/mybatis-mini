package com.vonchange.jdbc.springjdbc.repository;

import com.vonchange.jdbc.abstractjdbc.core.AbstractJbdcCore;
import com.vonchange.jdbc.abstractjdbc.dialect.Dialect;
import com.vonchange.jdbc.abstractjdbc.dialect.MySQLDialect;
import com.vonchange.jdbc.abstractjdbc.model.DataSourceWrapper;

public  abstract   class AbstractJbdcRepositoryMysql extends AbstractJbdcCore {

    @Override
    protected Dialect getDefaultDialect() {
        return new MySQLDialect();
    }

    @Override
    protected DataSourceWrapper getDataSourceFromSql(String sql){
         return null;
    }

    @Override
    protected boolean needInitEntityInfo() {
        return true;
    }

}
