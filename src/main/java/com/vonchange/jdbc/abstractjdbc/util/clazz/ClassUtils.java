package com.vonchange.jdbc.abstractjdbc.util.clazz;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 类组件
 * 
 * @author von_change@163.com
 * @date 2015-6-14 下午10:28:34
 */
public class ClassUtils {
	private ClassUtils() {
		throw new IllegalStateException("Utility class");
	}
	/**
	 * 判断一个类是否为基本数据类型。
	 * 
	 * @param clazz
	 *            要判断的类。
	 * @return true 表示为基本数据类型。
	 */
	public static boolean isBaseType(Class<?> clazz) {
		return (clazz.equals(String.class) || clazz.equals(Integer.class) || clazz.equals(Byte.class) || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class) || clazz.equals(Character.class) || clazz.equals(Short.class) || clazz.equals(BigDecimal.class) || clazz.equals(BigInteger.class) || clazz.equals(Boolean.class) || clazz.equals(Date.class) || clazz.isPrimitive());
	}

}
