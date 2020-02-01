package com.vonchange.jdbc.abstractjdbc.core;


import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.jdbc.abstractjdbc.dialect.Dialect;
import com.vonchange.jdbc.abstractjdbc.model.SqlFragment;
import com.vonchange.jdbc.abstractjdbc.model.SqlParmeter;
import com.vonchange.jdbc.abstractjdbc.util.sql.OrmUtil;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.tpl.EntityUtil;
import com.vonchange.mybatis.tpl.model.EntityField;
import com.vonchange.mybatis.tpl.model.EntityInfo;
import jodd.bean.BeanUtil;
import jodd.bean.BeanUtilBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *抽象的HeaDao：包含增删改查
 * @author von_change@163.com
 * @date 2015-6-14 下午6:36:40
 * @param
 */
public abstract class AbstractJdbcCud {
	public static final Map<String, EntityInfo> entityMap = EntityUtil.entityMap;
	private static final Logger logger = LoggerFactory.getLogger(AbstractJdbcCud.class);
	protected abstract Object insert(String sql, Object[] parameter);
	protected abstract int update( String sql, Object... args);
	protected  abstract Dialect getDefaultDialect();
	public final <T> T  saveEntity(T entity) {
		SqlParmeter sqlParmeter = generateInsertSql(entity,false);
		Object id=insert(sqlParmeter.getSql(), sqlParmeter.getParameters());
		if(null!=id){
			BeanUtil beanUtil = new BeanUtilBean();
			beanUtil.setProperty(entity, sqlParmeter.getIdName(), id);
		}
		return entity;
	}
	public final <T> int  updateEntity(T entity) {
		SqlParmeter sqlParmeter = generateUpdateEntitySql(entity);
		return  update(sqlParmeter.getSql(), sqlParmeter.getParameters());
	}
	public final <T> int  updateEntity(T entity,String... nullFields) {
		SqlParmeter sqlParmeter = generateUpdateEntitySql(entity,nullFields);
		return  update(sqlParmeter.getSql(), sqlParmeter.getParameters());
	}
	public final <T> T  saveDuplicateKeyEntity(T entity) {
		SqlParmeter sqlParmeter = generateInsertSql(entity,true);
		Object id=insert(sqlParmeter.getSql(), sqlParmeter.getParameters());
		if(null!=id){
			BeanUtil beanUtil = new BeanUtilBean();
			beanUtil.setProperty(entity, sqlParmeter.getIdName(), id);
		}
		return entity;
	}
	protected void initEntityInfo(Class<?> clazz) {
		EntityUtil.initEntityInfo(clazz);
	}


	public static Object getPublicPro(Object bean, String name) {
		if(name.equals("serialVersionUID")){//??????
			return null;
		}
		BeanUtil beanUtil  = new BeanUtilBean();
		return beanUtil.getProperty(bean, name);
	}
	protected <T> SqlParmeter generateInsertSql(T entity,boolean duplicate) {
		initEntityInfo(entity.getClass());
		SqlParmeter sqlParmeter = new SqlParmeter();
		List<String> columnList = new ArrayList<>();
		List<Object> valueList = new ArrayList<>();

		List<String> columnListUpdate = new ArrayList<>();
		List<Object> valueListUpdate = new ArrayList<>();

		String entityName = entity.getClass().getSimpleName();
		EntityInfo entityInfo = entityMap.get(entityName);
		String tableName = entityInfo.getTableName();
		Map<String, EntityField> entityFieldMap = entityMap.get(entityName).getFieldMap();
		String columnSql;
		for (Map.Entry<String, EntityField> entry : entityFieldMap.entrySet()) {
			String fieldName = entry.getKey();
			EntityField entityField = entry.getValue();
			if (entityField.getIsColumn()) {
				Object value =getPublicPro(entity, fieldName);
				if (null != value) {
					Dialect dialect =getDefaultDialect();
					columnSql="`"+entityField.getColumnName()+"`";
					if(dialect.getDialogName().equals(ConstantJdbc.Dialog.ORACLE)){
						columnSql=entityField.getColumnName();
					}
					columnList.add(columnSql);
					valueList.add(value);
					if(duplicate&&!entityField.getIgnoreUpdate()){
						columnListUpdate.add(columnSql);
						valueListUpdate.add(value);
					}
				}
			}
		}
		Dialect dialect =getDefaultDialect();
		String ext="@mysql";
		if(dialect.getDialogName().equals(ConstantJdbc.Dialog.ORACLE)){
			ext="@oracle";
		}
		String insertSql = StringUtils.format("insert into {0}({1}) values ({2}) /* {3}  */", tableName, StringUtils.strList(columnList, ","), StringUtils.strNums("?", ",", columnList.size()),ext);
		if(duplicate){
			insertSql=insertSql+" ON DUPLICATE KEY UPDATE "+getSetSql(columnListUpdate);
		}
		String idName = entityInfo.getIdFieldName();
		sqlParmeter.setIdName(idName);
		sqlParmeter.setSql(insertSql);
		if(!duplicate){
			sqlParmeter.setParameters(valueList.toArray());
		}else{
            List<Object> allList= new ArrayList<>();
            allList.addAll(valueList);
            allList.addAll(valueListUpdate);
			sqlParmeter.setParameters(allList.toArray());
		}
		return sqlParmeter;
	}
	protected <T> SqlParmeter generateUpdateEntitySql(T entity,String... nullFields) {
		SqlParmeter sqlParmeter = new SqlParmeter();
		initEntityInfo(entity.getClass());
		String entityName = entity.getClass().getSimpleName();
		EntityInfo entityInfo = entityMap.get(entityName);
		String tableName = entityInfo.getTableName();
		String idColumnName=entityInfo.getIdColumnName();
		Object idValue =getPublicPro(entity, entityInfo.getIdFieldName());
		if(null==idValue){
			throw new RuntimeException("主键值为空！无法更新");
		}
		SqlFragment setSqlEntity =getUpdateSetSql(entity, entityInfo,nullFields);
		String setSql=setSqlEntity.getSql();
		List<Object> params= setSqlEntity.getParams();
		params.add(idValue);
		//0:tableName 1:setSql 2:idName
		String sql = StringUtils.format("update {0} set {1} where {2}=?",tableName,setSql,idColumnName);
		sqlParmeter.setSql(sql);
		sqlParmeter.setParameters(params.toArray());
		return sqlParmeter;
	}
	private <T> SqlFragment getUpdateSetSql(T entity, EntityInfo entityInfo,String... nullFields) {
		SqlFragment sqlFragment = new SqlFragment();
		List<String> setColumnList = new ArrayList<String>();
		List<Object> params = new ArrayList<Object>();
		Map<String, EntityField> entityFieldMap = entityInfo.getFieldMap();
		for (Map.Entry<String, EntityField> entry : entityFieldMap.entrySet()) {
			String fieldName = entry.getKey();
			EntityField entityField = entry.getValue();
			if (entityField.getIsColumn()) {
				if (!entityField.getIsId()) {
					Object value =getPublicPro(entity, fieldName);
					if (null != value) {
						setColumnList.add(StringUtils.format("{0} =?", entityField.getColumnName()));
						params.add(value);
					}
				}
			}
		}
		if(null!=nullFields&&nullFields.length>0){
			for (String fieldStr : nullFields) {
				String columnName = OrmUtil.toSql(fieldStr);
				setColumnList.add(StringUtils.format("{0} =?", columnName));
				params.add(null);
			}
		}
		String sql = StringUtils.StrList(setColumnList, ",");
		sqlFragment.setSql(sql);
		sqlFragment.setParams(params);
		return sqlFragment;
	}


	private  String getSetSql(	List<String> columnList){
		StringBuilder sql=new StringBuilder();
		for (String column: columnList ) {
			sql.append(column+"=?,");
		}
		return  sql.substring(0,sql.length()-1);
	}

	

}
