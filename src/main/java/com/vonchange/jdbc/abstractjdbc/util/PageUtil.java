package com.vonchange.jdbc.abstractjdbc.util;

/**
 * Created by 冯昌义 on 2018/5/24.
 */
public class PageUtil {
    public static int getTotalPage(long totalNum, int pageSize) {
        if (pageSize <= 0 || totalNum <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalNum / (double) pageSize);
    }
}
