package com.vonchange.jdbc.abstractjdbc.core;


import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.jdbc.abstractjdbc.config.Constants;
import com.vonchange.jdbc.abstractjdbc.dialect.Dialect;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractMapPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractPageWork;
import com.vonchange.jdbc.abstractjdbc.model.DataSourceWrapper;
import com.vonchange.jdbc.abstractjdbc.model.SqlFragment;
import com.vonchange.jdbc.abstractjdbc.model.SqlParmeter;
import com.vonchange.jdbc.abstractjdbc.parser.CountSqlParser;
import com.vonchange.jdbc.abstractjdbc.template.YhJdbcTemplate;
import com.vonchange.jdbc.abstractjdbc.util.SqlUtil;
import com.vonchange.jdbc.abstractjdbc.util.markdown.MarkdownUtil;
import com.vonchange.jdbc.abstractjdbc.util.markdown.bean.SqlInfo;
import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.config.Constant;
import com.vonchange.mybatis.tpl.EntityUtil;
import com.vonchange.mybatis.tpl.model.EntityField;
import com.vonchange.mybatis.tpl.model.EntityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * jdbc core
 * by von_change
 */
public abstract class AbstractJbdcCore implements JdbcRepostitory{


    private static final Logger logger = LoggerFactory.getLogger(AbstractJbdcCore.class);
    private static final Map<String, EntityInfo> entityMap = EntityUtil.entityMap;


    protected abstract Dialect getDefaultDialect();
    protected abstract DataSourceWrapper getReadDataSource();
    protected abstract DataSourceWrapper getWriteDataSource();

    protected abstract  DataSourceWrapper getDataSourceFromSql(String sql);

    private static volatile IJdbcBase jdbcBase= null;
    private   IJdbcBase getJdbcBase(){
        if (null == jdbcBase) {
            synchronized (JdbcBaseImpl.class) {
                if (null == jdbcBase) {
                    jdbcBase = new JdbcBaseImpl() {
                        @Override
                        protected YhJdbcTemplate initJdbcTemplate(String sql,Constants.EnumRWType enumRWType) {
                            return  getJdbcTemplate(sql,enumRWType);
                        }
                    };
                }
            }
        }
        return jdbcBase;
    }


    private  static Map<String,YhJdbcTemplate> yhJdbcTemplateMap=new ConcurrentHashMap<>();
    private YhJdbcTemplate getJdbcTemplate(String sql,Constants.EnumRWType enumRWType) {
        DataSourceWrapper dataSource = getWriteDataSource();
        if(enumRWType.equals(Constants.EnumRWType.read)){
            dataSource = getReadDataSource();
        }
        //从sql 里 获取datasource  实例名
        DataSourceWrapper dataSourceFromSql =  getDataSourceFromSql(sql);
        if(null!=dataSourceFromSql){
            dataSource=dataSourceFromSql;
        }

        if(yhJdbcTemplateMap.containsKey(dataSource.getKey())){
            return yhJdbcTemplateMap.get(dataSource.getKey());
        }
        //缓存？
        YhJdbcTemplate yhJdbcTemplate = new YhJdbcTemplate(dataSource.getDataSource());
        yhJdbcTemplate.setFetchSizeBigData(getDefaultDialect().getBigDataFetchSize());
        yhJdbcTemplate.setFetchSize(getDefaultDialect().getFetchSize());
        yhJdbcTemplateMap.put(dataSource.getKey(),yhJdbcTemplate);
        return yhJdbcTemplate;
    }



    public final <T> T  save(T entity) {
        SqlParmeter sqlParmeter = generateInsertSql(entity,false);
        Object id=getJdbcBase().insert(sqlParmeter.getSql(), sqlParmeter.getParameters());
        if(null!=id){
            Constant.BeanUtils.setProperty(entity, sqlParmeter.getIdName(), id);
        }
        return entity;
    }
    public final <T> int  update(T entity) {
        SqlParmeter sqlParmeter = generateUpdateEntitySql(entity,false);
        return  getJdbcBase().update(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }
    public final <T> int  updateAll(T entity) {
        SqlParmeter sqlParmeter = generateUpdateEntitySql(entity,true);
        return  getJdbcBase().update(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }
    public   <T> T  saveDuplicateKey(T entity) {
        SqlParmeter sqlParmeter = generateInsertSql(entity,true);
        Object id=getJdbcBase().insert(sqlParmeter.getSql(), sqlParmeter.getParameters());
        if(null!=id){
            Constant.BeanUtils.setProperty(entity, sqlParmeter.getIdName(), id);
        }
        return entity;
    }
    private void initEntityInfo(Class<?> clazz) {
        EntityUtil.initEntityInfo(clazz);
    }


    public static Object getPublicPro(Object bean, String name) {
        if(name.equals("serialVersionUID")){//??????
            return null;
        }
        return Constant.BeanUtils.getProperty(bean, name);
    }
    private  <T> SqlParmeter generateInsertSql(T entity,boolean duplicate) {
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
                    if(duplicate&&!entityField.getUpdateNotNull()){
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
    private  <T> SqlParmeter generateUpdateEntitySql(T entity,boolean isNullUpdate) {
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
        SqlFragment setSqlEntity =getUpdateSetSql(entity, entityInfo, isNullUpdate);
        String setSql=setSqlEntity.getSql();
        List<Object> params= setSqlEntity.getParams();
        params.add(idValue);
        //0:tableName 1:setSql 2:idName
        String sql = StringUtils.format("update {0} set {1} where {2}=?",tableName,setSql,idColumnName);
        sqlParmeter.setSql(sql);
        sqlParmeter.setParameters(params.toArray());
        return sqlParmeter;
    }
    private <T> SqlFragment getUpdateSetSql(T entity, EntityInfo entityInfo,boolean isNullUpdate) {
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
                    if(null==value&&isNullUpdate&&!entityField.getUpdateNotNull()){
                        setColumnList.add(StringUtils.format("{0} =?", entityField.getColumnName()));
                        params.add(null);
                    }
                }
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
    //crud end
    public <T> T queryById(Class<T> type, Object id) {
        String sql =generateQueryByIdSql(type);
        return getJdbcBase().queryOne(type, sql,id);
    }
    private String generateQueryByIdSql(Class<?> type){
        initEntityInfo(type);
        EntityInfo entityInfo = entityMap.get(type.getSimpleName());
        String tableName = entityInfo.getTableName();
        if(null==tableName){
            throw new RuntimeException("未定义实体table注解");
        }
        String idName = entityInfo.getIdColumnName();
        return StringUtils.format("select * from {0} where  {1} = ?",tableName,idName);
    }
    public <T> List<T> queryList(Class<T> type, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        SqlParmeter sqlParmeter = getSqlParmeter(sqlinfo.getSql(), parameter);
        return findList(type, sqlParmeter.getSql(), sqlParmeter.getParameters());
    }


    private  <T> List<T> findList(Class<T> type, String sql, Object... args) {
        return getJdbcBase().queryList(type, sql, args);
    }


    public <T> T queryOne(Class<T> type, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        return findBean(type, sqlinfo.getSql(), parameter);
    }


    public <T> Page<T> queryPage(Class<T> type, String sqlId, Pageable pageable, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        SqlInfo countSqlInfo = getSqlInfo(sqlId + ConstantJdbc.COUNTFLAG);
        String countSql = countSqlInfo.getSql();
        if (StringUtils.isBlank(countSqlInfo.getSql())) {
            countSql = null;
        }
        return findPage(type, sqlinfo.getSql(), countSql, pageable, parameter);
    }


    private  <T> T findBean(Class<T> type, String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findBean(type, sqlParmeter.getSql(), sqlParmeter.getParameters());
    }


    private  <T> T findBean(Class<T> type, String sql, Object... args) {
        sql = removeLimit(sql);
        sql = getDefaultDialect().getLimitOne(sql);
        return getJdbcBase().queryOne(type, sql, args);
    }


    private List<Map<String, Object>> queryListBySql(String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findList(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }

    public Map<String, Object> queryOne(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        return findOne(sqlinfo.getSql(), parameter);
    }

    private Map<String, Object> findOne(String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findOne(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }


    private Map<String, Object> findOne(String sql, Object... args) {
        sql = removeLimit(sql);
        sql = getDefaultDialect().getLimitOne(sql);
        return getJdbcBase().queryUniqueResultMap(sql, args);
    }

    private List<Map<String, Object>> findList(String sql, Object... args) {
        return getJdbcBase().queryListResultMap(sql, args);
    }


    public final Page<Map<String, Object>> queryPageBigData(String sqlId, AbstractMapPageWork pageWork, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return findBigData(sqlInfo.getSql(), pageWork, parameter);
    }

    private Page<Map<String, Object>> findBigData(String sql, AbstractMapPageWork pageWork, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findBigData(sqlParmeter.getSql(), pageWork, sqlParmeter.getParameters());
    }

    private Page<Map<String, Object>> findBigData(String sql, AbstractMapPageWork pageWork, Object... args) {
        return getJdbcBase().queryForBigData(sql, pageWork, args);
    }

    public final <T> Page<T> queryPageBigData(Class<T> type, String sqlId, AbstractPageWork pageWork, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return findBigData(type, sqlInfo.getSql(), pageWork, parameter);
    }

    private  <T> Page<T> findBigData(Class<T> type, String sql, AbstractPageWork pageWork, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findBigData(type, sqlParmeter.getSql(), pageWork, sqlParmeter.getParameters());
    }

    private  <T> Page<T> findBigData(Class<T> type, String sql, AbstractPageWork pageWork, Object... args) {
        return getJdbcBase().queryForBigData(type, sql, pageWork, args);
    }


    public final Page<Map<String, Object>> queryPage(String sqlId, Pageable pageable, Map<String, Object> parameter) {

        SqlInfo sqlInfo = getSqlInfo(sqlId);
        SqlInfo countSqlInfo = getSqlInfo(sqlId + ConstantJdbc.COUNTFLAG);
        String countSql = countSqlInfo.getSql();
        if (StringUtils.isBlank(countSql)) {
            countSql = null;
        }
        return findPage(sqlInfo.getSql(), countSql, pageable, parameter);
    }

    public final List<Map<String, Object>> queryList(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return findList(sqlInfo.getSql(), parameter);
    }


    private  Page<Map<String, Object>> findPage(String sql, String countSql, Pageable pageable, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        sql = removeLimit(sql);
        long totalCount = countMySqlResult(sql, countSql, parameter);
        return findPage(removeLimit(sqlParmeter.getSql()), totalCount, pageable, sqlParmeter.getParameters());
    }

    private <T> Page<T> findPage(Class<T> type, String sql, String countSql, Pageable pageable, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        sql = removeLimit(sql);
        long totalCount = countMySqlResult(sql, countSql, parameter);
        return findPage(type, removeLimit(sqlParmeter.getSql()), totalCount, pageable, sqlParmeter.getParameters());
    }

    private String removeLimit(String sql) {
        String lowerSql = sql.toLowerCase();
        if (lowerSql.contains("limit ")) {
            sql = sql.substring(0, lowerSql.indexOf("limit "));
        }
        if (lowerSql.contains("limit\n")) {
            sql = sql.substring(0, lowerSql.indexOf("limit\n"));
        }
        return sql;
    }

    private <T> Page<T> findPage(Class<T> type, String sql, long totalCount, Pageable pageable, Object... parameter) {

        int pageNum = pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber();
        int firstEntityIndex = pageable.getPageSize() * pageNum;
        sql = getDefaultDialect().getPageSql(sql, firstEntityIndex, pageable.getPageSize());
        List<T> entities = findList(type, sql, parameter);
        return new PageImpl<>(entities, pageable, totalCount);
    }

    private Page<Map<String, Object>> findPage(String sql, long totalCount, Pageable pageable, Object... parameter) {
      /*  sql = removeLimit(sql);
        long totalCount = countMySqlResult(sql, countSql, parameter);*/
        int pageNum = pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber();
        int firstEntityIndex = pageable.getPageSize() * pageNum;
        sql = getDefaultDialect().getPageSql(sql, firstEntityIndex, pageable.getPageSize());
        List<Map<String, Object>> entities = findList(sql, parameter);
        return new PageImpl<>(entities, pageable, totalCount);
    }

    private Page<Map<String, Object>> findPage(String sql, String countSql, Pageable pageable, Object... parameter) {
        sql = removeLimit(sql);
        long totalCount = countMySqlResultInArray(sql, countSql, parameter);
        return findPage(sql, totalCount, pageable, parameter);
    }

    private final Page<Map<String, Object>> findPage(String sql, Pageable pageable, Map<String, Object> parameter) {
        return findPage(sql, null, pageable, parameter);
    }

    private Page<Map<String, Object>> findPage(String sql, Pageable pageable, Object... parameter) {
        return findPage(sql, null, pageable, parameter);
    }

    private long countMySqlResult(String sql, String countSql, Map<String, Object> params) {
        if (null != params.get(ConstantJdbc.PageParam.COUNT)) {
            countSql = ConvertUtil.toString(params.get(ConstantJdbc.PageParam.COUNT));
        }
        if (!StringUtils.isBlank(countSql) && countSql.startsWith(ConstantJdbc.PageParam.AT)) {
            return ConvertUtil.toLong(countSql.substring(1));
        }
        Object result = null;
        if (StringUtils.isBlank(countSql)) {
            SqlParmeter sqlParmeter = getSqlParmeter(sql, params);
            countSql = generateMyCountSql(sqlParmeter.getSql());
            result = findBy(countSql, 1, sqlParmeter.getParameters());
        }
        if (null == result) {
            return 0L;
        }
        return ConvertUtil.toLong(result);
    }
    public long countResult(String sqlId,Map<String, Object> params) {
         SqlInfo sqlInfo= getSqlInfo(sqlId);
         return countMySqlResult(sqlInfo.getSql(),null,params);
    }

    private long countMySqlResultInArray(String sql, String countSql, Object... params) {
        if (!StringUtils.isBlank(countSql) && countSql.startsWith(ConstantJdbc.PageParam.AT)) {
            return ConvertUtil.toLong(countSql.substring(1));
        }
        if (StringUtils.isBlank(countSql)) {
            countSql = generateMyCountSql(sql);
        }
        Object result = findBy(countSql, 1, params);
        if (null == result) {
            return 0L;
        }
        return ConvertUtil.toLong(result);
    }


    public  <T> T queryOneField(Class<?> targetType,String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);

        Object result= findBy(sqlInfo.getSql(), parameter);
        return ConvertUtil.toObject(result,targetType);
    }

    private Object findBy(String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findBy(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }

    private Object findBy(String sql, Object... params) {
        return findBy(sql, 1, params);
    }

    private Object findBy(String sql, int columnIndex, Object... params) {
        return getJdbcBase().queryOneColumn(sql, columnIndex, params);
    }

    private SqlParmeter getSqlParmeter(String sql, Map<String, Object> parameter) {
        return  SqlUtil.getSqlParmeter(sql, parameter);
    }


    public <T> Map<String, T> queryToMap(Class<T> c, String sqlId, String keyInMap, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return queryBySql(c, sqlInfo.getSql(), keyInMap, parameter);
    }

    private  <T> Map<String, T> queryBySql(Class<T> c, String sql, String keyInMap, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        Map<String, T> result = getJdbcBase().queryMapList(c, sqlParmeter.getSql(), keyInMap, sqlParmeter.getParameters());
        return result;
    }

    private SqlInfo getSqlInfo(String sqlId) {
      /*  String modulePath=modulePath();
        if(!sqlId.contains(".")&&!StringUtils.isBlank(modulePath)){
            sqlId=modulePath+"."+sqlId;
        }*/
        return MarkdownUtil.getSqlInfo(sqlId);
    }


    private String generateMyCountSql(String sql) {
        StringBuilder sb= new StringBuilder();
        Matcher m = Pattern.compile("(/\\*)([\\w\\s\\@\\:]*)(\\*/)").matcher(sql);
        while (m.find()) {
            String group = m.group();
            sb.append(group);
        }
        CountSqlParser countSqlParser = new CountSqlParser();
        return sb.toString()+countSqlParser.getSmartCountSql(sql);
    }


  /*  private void logSql(String sql, Object... params) {
        logger.info("\n原始sql为:\n{}\n参数为:{}\n生成的sql为:\n{}", sql, params, SqlFill.fill(sql, params));
    }*/


    public int update(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        String sql = sqlinfo.getSql();
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return getJdbcBase().update(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }

    public Object insert(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        String sql = sqlinfo.getSql();
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return getJdbcBase().insert(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }

}
