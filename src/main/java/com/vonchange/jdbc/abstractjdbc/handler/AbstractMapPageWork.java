package com.vonchange.jdbc.abstractjdbc.handler;

import java.util.List;
import java.util.Map;

/**
 * Created by 冯昌义 on 2018/4/17.
 */
public abstract class AbstractMapPageWork {
    protected abstract  void doPage(List<Map<String,Object>> pageContentList, int pageNum, Map<String,Object> extData);
    protected abstract  int  getPageSize();
}
