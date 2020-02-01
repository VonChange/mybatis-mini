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


import com.vonchange.jdbc.abstractjdbc.util.sql.SqlCommentUtil;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <core>ResultSetHandler</core> implementation that converts the first
 * <core>ResultSet</core> row into a <core>Map</core>. This class is thread
 * safe.
 */
public class MapListHandler implements ResultSetExtractor<List<Map<String,Object>>> {
    private  String sql;
    public MapListHandler(String sql){
    	this.sql=sql;
	}
	@Override
	public List<Map<String,Object>> extractData(ResultSet rs) throws SQLException {
		return this.toMapList(rs);
	}

	private List<Map<String,Object>> toMapList(ResultSet rs) throws SQLException {
		List<Map<String,Object>> result = new ArrayList<>();
		if (!rs.next()) {
			return result;
		}
		do {
			result.add(HandlerUtil.rowToMap(rs, SqlCommentUtil.getLower(sql),
					SqlCommentUtil.getOrm(sql)));
		} while (rs.next());
		return result;
	}
}
