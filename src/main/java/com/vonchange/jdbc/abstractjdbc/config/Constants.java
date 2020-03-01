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
	public  enum EnumRWType {
		read(0,"读"),write(0,"写");

		private Integer value;
		private String desc;
		EnumRWType(Integer value, String desc){
			this.value=value;
			this.desc=desc;
		}
		public static EnumRWType getValue(Integer value) {
			for (EnumRWType c : EnumRWType.values()) {
				if (c.getValue().equals(value)) {
					return c;
				}
			}
			return null;
		}
		public Integer getValue() {
			return value;
		}

		public EnumRWType setValue(int value) {
			this.value = value;
			return this;

		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}
	}

}
