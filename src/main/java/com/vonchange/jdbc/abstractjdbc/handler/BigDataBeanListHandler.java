/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vonchange.jdbc.abstractjdbc.handler;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @param <T> the target processor type
 */
public  class BigDataBeanListHandler<T> implements ResultSetExtractor<Page<T>> {

    /**
     * The Class of beans produced by this handler.
     */
    private final Class<? extends T> type;
    private   AbstractPageWork abstractPageWork;
    private  String sql;

    /**
     * Creates a new instance of BeanListHandler.
     *
     * @param type The Class that objects returned from <core>handle()</core>
     * are created from.
     */


    /**
     * Creates a new instance of BeanListHandler.
     *
     * @param type The Class that objects returned from <core>handle()</core>
     * are created from.
     * to use when converting rows into beans.
     */
    public BigDataBeanListHandler(Class<? extends T> type, AbstractPageWork abstractPageWork,String sql) {
        this.type = type;
        this.abstractPageWork=abstractPageWork;
        this.sql=sql;
    }

    /**
     * Convert the whole <core>ResultSet</core> into a List of beans with
     * the <core>Class</core> given in the constructor.
     *
     * @param rs The <core>ResultSet</core> to handle.
     *
     * @return A List of beans, never <core>null</core>.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Page<T> extractData(ResultSet rs) throws SQLException {
        int pageSize=abstractPageWork.getPageSize();
        try {
            return this.toBeanList(rs, type,pageSize);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return new PageImpl<>(new ArrayList<T>());
    }
    private  Page<T> toBeanList(ResultSet rs,  Class<? extends T> type,int pageSize) throws SQLException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<T> result = new ArrayList<>();
        if (!rs.next()) {
            abstractPageWork.doPage(result,0);
            return 	new PageImpl<>(result);
        }
        int pageItem=0;
        int pageNum=0;
        long count=0;
        BeanProcessor beanProcessor =new BeanProcessor();
        ResultSetMetaData rsmd = rs.getMetaData();
        do {
            result.add(beanProcessor.createBean(rs, rsmd, type));
            pageItem++;
            count++;
            if(pageItem==pageSize){
                abstractPageWork.doPage(result,pageNum);
                pageNum++;
                result=new ArrayList<>();
                pageItem=0;
            }
        } while (rs.next());
        if(result.size()>0){
            abstractPageWork.doPage(result,pageNum);
            Pageable pageable=new PageRequest(pageNum,pageSize);
            return 	new PageImpl<>(result,pageable,count);
        }
        Pageable pageable=new PageRequest(pageNum-1,pageSize);
        return 	new PageImpl<>(result,pageable,count);
    }
	
}