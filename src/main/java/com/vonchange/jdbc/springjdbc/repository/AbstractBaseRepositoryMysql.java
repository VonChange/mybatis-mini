package com.vonchange.jdbc.springjdbc.repository;

import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.jdbc.abstractjdbc.core.AbstractJbdcCore;
import com.vonchange.jdbc.abstractjdbc.dialect.Dialect;
import com.vonchange.jdbc.abstractjdbc.dialect.MySQLDialect;

public abstract class AbstractBaseRepositoryMysql extends AbstractJbdcCore {


    protected String getDataSource() {
        return ConstantJdbc.DataSource.DEFAULT;
    }


    protected Dialect getDefaultDialect() {
        return new MySQLDialect();
    }
}
