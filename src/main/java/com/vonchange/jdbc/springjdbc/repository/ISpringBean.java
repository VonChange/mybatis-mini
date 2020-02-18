package com.vonchange.jdbc.springjdbc.repository;

public interface ISpringBean {
    //通过name获取 Bean.
    <T> T getBean(String name);
}
