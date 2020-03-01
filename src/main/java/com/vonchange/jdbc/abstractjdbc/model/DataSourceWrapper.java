package com.vonchange.jdbc.abstractjdbc.model;

import javax.sql.DataSource;

public class DataSourceWrapper {
    private DataSource dataSource;
    private String key;
    public DataSourceWrapper(DataSource dataSource,String key){
        this.dataSource=dataSource;
        this.key=key;
    }

    public DataSource getDataSource() {
        return dataSource;
    }


    public String getKey() {
        return key;
    }

}
