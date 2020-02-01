package com.vonchange.jdbc.abstractjdbc.handler;

import java.util.List;

/**
 *
 * Created by 冯昌义 on 2018/4/17.
 */
public abstract class AbstractPageWork<T> {
    protected abstract   void doPage(List<T> pageContentList, int pageNum);
    protected abstract  int  getPageSize();
}
