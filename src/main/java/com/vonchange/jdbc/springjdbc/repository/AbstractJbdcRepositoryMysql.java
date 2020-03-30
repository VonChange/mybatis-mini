package com.vonchange.jdbc.springjdbc.repository;


import com.vonchange.jdbc.abstractjdbc.core.AbstractJdbcCore;
import com.vonchange.jdbc.abstractjdbc.dialect.Dialect;
import com.vonchange.jdbc.abstractjdbc.dialect.MySQLDialect;
import com.vonchange.jdbc.abstractjdbc.model.DataSourceWrapper;

public  abstract   class AbstractJbdcRepositoryMysql extends AbstractJdbcCore {

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
    protected boolean  readAllScopeOpen(){
        return false;
    }

}
