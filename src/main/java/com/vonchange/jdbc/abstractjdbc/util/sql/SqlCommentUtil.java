package com.vonchange.jdbc.abstractjdbc.util.sql;

/**
 * Created by 冯昌义 on 2018/4/17.
 */
public class SqlCommentUtil {

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
        public static final  String MYSQL="mysql";
        public static final  String ORACLE="oracle";
        public static final  String BASE="base";
    }
   /* public static    String  getDialect(String sql){
        if(sql.contains("@mysql")){
            return SqlCommentUtil.Dialect.MYSQL;
        }
        if(sql.contains("@oracle")){
            return SqlCommentUtil.Dialect.ORACLE;
        }
        if(sql.contains("@base")){
            return SqlCommentUtil.Dialect.BASE;
        }
        return   SqlCommentUtil.Dialect.MYSQL;
    }*/
}
