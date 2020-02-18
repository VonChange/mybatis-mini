package com.vonchange.jdbc.abstractjdbc.util.sql;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 填充sql
 *
 * @author von_change@163.com
 * @date 2016年4月6日 下午6:07:00
 * @since 1.0
 */
public class SqlFill {

    private static DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
    private static String getParameterValue(Object obj) {
        if(null==obj){
            return "''";
        }
        if(obj instanceof Date){
            return "'" + formatter.format(obj) + "'";
        }
        if (obj instanceof String) {
            //value = obj.toString().replaceAll("([';])+|(--)+", "");
            // 排除恶意sql漏洞
            //return "'" + obj.toString().replaceAll("([';])+|(--)+", "") + "'";
            return "'" + obj.toString() + "'";
        }
        return "'" + obj.toString() + "'";
    }

    public static String fill(String source, Object[] params) {
        if (null == params || params.length == 0) {
            return source;
        }
        //[ \x0B]+ "[\s]+
        String sql = source.replaceAll("[ \\x0B]+", " ");//去掉多余空格
        char[] chars = sql.toCharArray();
        StringBuilder sb = new StringBuilder();
        char j;
        int p = 0;
        for (int i = 0; i < chars.length; i++) {
            j = chars[i];
            if (j == '?') {
                sb.append(getParameterValue(params[p]));//
                p++;
            } else {
                sb.append(j);
            }
        }
        return sb.toString();
    }

}
