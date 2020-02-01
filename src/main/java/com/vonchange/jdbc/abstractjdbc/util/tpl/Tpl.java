package com.vonchange.jdbc.abstractjdbc.util.tpl;


import com.vonchange.jdbc.abstractjdbc.model.SqlParmeter;

import java.util.Map;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2017/12/12.
 */
public interface Tpl {
    public SqlParmeter generate(
            Map<String, Object> data, String tplStr);
}
