package com.vonchange.jdbc.abstractjdbc.model;

import java.util.List;

/**
 *sql和参数
 * @author von_change@163.com
 * 2015-6-14 下午12:45:54
 */
public class SqlParmeter {
    private String sql;
    private Object[] parameters;
    private String idName;
    private List<String> propertyNames;

	public List<String> getPropertyNames() {
		return propertyNames;
	}

	public void setPropertyNames(List<String> propertyNames) {
		this.propertyNames = propertyNames;
	}

	public String getSql() {
	return sql;
}
    public void setSql(String sql) {
	this.sql = sql;
}
    public Object[] getParameters() {
	return parameters;
}
    public void setParameters(Object[] parameters) {
	this.parameters = parameters;
}

	public String getIdName() {
		return idName;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}
}