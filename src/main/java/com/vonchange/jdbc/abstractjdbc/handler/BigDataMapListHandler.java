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


import com.vonchange.mybatis.tpl.sql.SqlCommentUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <core>ResultSetHandler</core> implementation that converts the first
 * <core>ResultSet</core> row into a <core>Map</core>. This class is thread
 * safe.
 */
public  class  BigDataMapListHandler implements ResultSetExtractor<Page<Map<String,Object>>> {
	private  final String sql;
	/**
	 * 0 正常 1 强制小写
	 */
	private   AbstractMapPageWork abstractPageWork;
	public BigDataMapListHandler(AbstractMapPageWork abstractPageWork,String sql) {
		this.sql=sql;
		this.abstractPageWork=abstractPageWork;
	}

	@Override
	public Page<Map<String,Object>> extractData(ResultSet rs) throws SQLException {
		int pageSize=abstractPageWork.getPageSize();
		return this.toMapList(rs,pageSize);
	}

	private Page<Map<String,Object>> toMapList(ResultSet rs,int pageSize) throws SQLException {
		List<Map<String,Object>> result = new ArrayList<>();
		Map<String,Object> extData= new HashMap<>();
		if (!rs.next()) {
			abstractPageWork.doPage(result,0,extData);
			return 	new PageImpl<>(result);
		}
		int pageItem=0;
		int pageNum=0;
		long count=0;
		do {
			result.add(HandlerUtil.rowToMap(rs, SqlCommentUtil.getLowerNo(sql)
					,SqlCommentUtil.getOrmNo(sql)));
			pageItem++;
			count++;
			if(pageItem==pageSize){
				abstractPageWork.doPage(result,pageNum,extData);
				pageNum++;
				result=new ArrayList<>();
				pageItem=0;
			}
		} while (rs.next());
		if(!result.isEmpty()){
			abstractPageWork.doPage(result,pageNum,extData);
			Pageable pageable=new PageRequest(pageNum,pageSize);
			return 	new PageImpl<>(result,pageable,count);
		}
		Pageable pageable=new PageRequest(pageNum-1,pageSize);
		return 	new PageImpl<>(result,pageable,count);
	}
}
