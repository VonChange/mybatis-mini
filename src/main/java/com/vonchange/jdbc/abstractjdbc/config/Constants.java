package com.vonchange.jdbc.abstractjdbc.config;


/**
 * 数据字典
 * @author song.chen
 *
 */
public class Constants {

	/**
	 * mardown 配置里的信息
	 */
	public static  class  Markdown{
		public static final String SQL = "";//sql:
		public static final String TABLE = "";
		public static final String VIEW = "";
		public static final String JSON = "json:";
		public static final String JS = "js:";
		public static final String TABLES = "tables";
		public static final String VIEWS = "views";
		public static final String IDPREF = "--";
		public static class Cache{
			public static final String CACHE= "cache";
			public static final String TIME = "time";
			public static final String SIZE = "size";
		}
	}

}
