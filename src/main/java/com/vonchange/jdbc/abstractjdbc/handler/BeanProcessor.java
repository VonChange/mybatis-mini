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


import com.vonchange.mybatis.tpl.exception.MyRuntimeException;
import com.vonchange.jdbc.abstractjdbc.util.ConvertMap;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 *bean处理器
 * @author von_change@163.com
 * @date 2015-6-14 下午10:12:33
 */
public class BeanProcessor {
	/**
	 * 创建bean
	 * @param rs
	 * @param rsmd
	 * @return T
	 * @throws SQLException
	 */
	public <T> T createBean(ResultSet rs, ResultSetMetaData rsmd, Class<T> c) throws SQLException, InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException {
		return (T) ConvertMap.convertMap(c,ConvertMap.newMap(HandlerUtil.rowToMap(rs)));
	}


	/**
	 *实例化bean
	 * @return T
	 */
	public <T> T newInstance(Class<? extends T> c) {
		try {
			return c.newInstance();
		} catch (InstantiationException e) {
			throw new MyRuntimeException("Cannot create " + c.getName() + ": " + e.getMessage());

		} catch (IllegalAccessException e) {
			throw new MyRuntimeException("Cannot create " + c.getName() + ": " + e.getMessage());
		}
	}

}
