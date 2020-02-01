package com.vonchange.jdbc.abstractjdbc.parser;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2018/1/3.
 */
//@SpringBootTest
public class CountSqlParserTest {
    //@Test
    public void getSmartCountSql() throws Exception {
        String sql="select userName  aa from a where 1=1  " +
                "order by id desc limit 10";/*rx*/
        sql="SELECT bank_country_key,bank_keys,record_created_date,created_object_name,bank_name,region,city,deletion_indicator,city_code,\n" +
                "`IF`(' '=bank_category,LEFT(bank_keys,3),bank_category) bank_category\n" +
                ",1  from mdm_bank_master_record where 1=1\n" +
                " \n" +
                "ORDER BY update_time,bank_country_key,bank_keys";
        CountSqlParser countSqlParser= new CountSqlParser();
        String result=countSqlParser.getSmartCountSql(sql);
        System.out.println(result);
    }
    //@Test
    public void getSmartCountSqlX() throws Exception {
        String sql="select userName  aa from a where 1=1  " +
                "order by id desc limit 10";// /*keep smart*/
        sql="/*keep smart*/ select * from (SELECT bank_country_key,bank_keys,record_created_date,created_object_name,bank_name,region,city,deletion_indicator,city_code,\n" +
                "`IF`(' '=bank_category,LEFT(bank_keys,3),bank_category) bank_category\n" +
                ",1  from mdm_bank_master_record where 1=1\n" +
                " \n" +
                "ORDER BY update_time,bank_country_key,bank_keys) temp group by id";

        System.out.println("");
    }

}