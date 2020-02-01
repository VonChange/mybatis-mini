package com.vonchange.jdbc.abstractjdbc.exception;


import com.vonchange.mybatis.tpl.exception.MyRuntimeException;

/**
 *不允许使用数据库字段!
 * @author von_change@163.com
 * @date 2015-6-14 下午12:50:21
 */
public class NotAllowDBColumnException extends MyRuntimeException {

	public NotAllowDBColumnException(String msg) {
		super(msg);
	}
}
