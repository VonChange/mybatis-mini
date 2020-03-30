package com.vonchange.jdbc.abstractjdbc.util.sql;


import com.vonchange.jdbc.abstractjdbc.exception.NotAllowDBColumnException;
import jodd.util.StringUtil;


/**
 * Orm组件
 * 
 * @author von_change@163.com
 * 2015-6-14 下午1:02:52
 */
public class OrmUtil {
	/**
	 * _转大写
	 * 
	 * @param colName
	 */
	public static   String toFiled(String colName) {
		return  toUp(colName);
	}
	private static   String toUp(String colName){
		if(null==colName||!colName.contains("_")){
			return colName;
		}
		StringBuilder sb = new StringBuilder();
		boolean flag = false;
		for (int i = 0; i < colName.length(); i++) {
			char cur = colName.charAt(i);
			if (cur == '_') {
				flag = true;

			} else {
				if (flag) {
					sb.append(Character.toUpperCase(cur));
					flag = false;
				} else {
					sb.append(Character.toLowerCase(cur));
				}
			}
		}
		return sb.toString();
	}


	/**
	 * Hql转成sql
	 *
	 * @param myHql
	 * @return sql
	 */
	public static String toSql(String myHql) {
			StringBuilder sb = new StringBuilder();
			boolean flag = false;
			boolean isLetter = false;
			for (int i = 0; i < myHql.length(); i++) {
				char cur = myHql.charAt(i);
				if (cur == '_') {
					throw new NotAllowDBColumnException("不允许使用数据库字段  _ !");
				}
				if (cur == ':') {
					flag = true;
				}
				if (cur != ':' && !Character.isLetter(cur)) {
					flag = false;
				}
				if (flag) {
					sb.append(cur);
					continue;
				}
				if (Character.isUpperCase(cur) && isLetter) {
					sb.append("_");
					sb.append(Character.toLowerCase(cur));
				} else {
					sb.append(Character.toLowerCase(cur));
				}
				if (!Character.isLetter(cur)) {
					isLetter = false;
				} else {
					isLetter = true;
				}
			}
			return sb.toString();
	}

	/**
	 * 数据库表名转实体名
	 * 
	 * @param tableName
	 * @return 实体名
	 */
	public static String toEntity(String tableName) {
		return StringUtil.capitalize(toUp(tableName));
	}



}
