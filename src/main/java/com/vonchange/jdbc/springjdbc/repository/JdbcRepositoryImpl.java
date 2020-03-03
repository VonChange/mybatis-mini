package com.vonchange.jdbc.springjdbc.repository;

import com.vonchange.jdbc.abstractjdbc.core.JdbcRepository;
import com.vonchange.jdbc.abstractjdbc.model.DataSourceWrapper;

import javax.sql.DataSource;


public    class JdbcRepositoryImpl extends AbstractJbdcRepositoryMysql implements JdbcRepository {
    private DataSource dataSource;
    public JdbcRepositoryImpl(DataSource dataSource){
        this.dataSource=dataSource;
    }

    @Override
    protected DataSourceWrapper getReadDataSource() {
        return new DataSourceWrapper(dataSource,"dataSource");
    }

    @Override
    protected DataSourceWrapper getWriteDataSource() {
        return new DataSourceWrapper(dataSource,"dataSource");
    }



}
