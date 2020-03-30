package com.vonchange.jdbc.abstractjdbc.util.sql;

/**
 * Created by 冯昌义 on 2018/4/17.
 */
public class SqlCommentUtil {
    private SqlCommentUtil() { throw new IllegalStateException("Utility class"); }
    public static   boolean getLower(String sql){
        if(sql.contains("@lower")){
            return true;
        }
        return  false;
    }
    public static  boolean getOrm(String sql){
        if(sql.contains("@orm")){
            return true;
        }
        return  false;
    }
    public  static  class Dialect{
        private Dialect() { throw new IllegalStateException("Utility class"); }
        public static final  String MYSQL="mysql";
        public static final  String ORACLE="oracle";
        public static final  String BASE="base";
    }
}
