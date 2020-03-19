package com.vonchange.jdbc.abstractjdbc.config;

/**
 * Created by 冯昌义 on 2018/3/20.
 */
public class ConstantJdbc {

    public static  class  PageParam{
        public static final String COUNT = "_count";
        public static final String AT = "@";
    }
    public  static final String ISSQLFLAG= "@sql";
    public  static final String ISMDFLAG= "@md";
    public  static final String MAINFLAG= "main";
    public  static final String MDID= "### id";
    public  static final String MDVERSION= "#### version";
    public  static final String MDINITVERSION= "1.0";
    public static  class  DataSource{
        public static final String DEFAULT = "dataSource";
        public static final String DEFAULTES = "esDataSource";
        public static final String FLAG = "@ds:";
    }
    public static final String CACHEPRE = "sql_";
    public static final String COUNTFLAG = "Count";
    public static final String CACHEID = " @id";
    public static final String ENDSQL = "Sql";
    public static final String SQLFATH = "sql";
    public static final String MAPFIELDSPLIT = "#";
    public static final String THREADNUMNAME="spring.jdbc.thread";
    public static final  String THREADNUM="16";
    public static  class  Dialog{
        public static final String MYSQL = "mysql";
        public static final String ORACLE = "oracle";
    }
}
