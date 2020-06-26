package com.vonchange.jdbc.abstractjdbc.core;


import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.jdbc.abstractjdbc.config.Constants;
import com.vonchange.jdbc.abstractjdbc.count.CountSqlParser;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractMapPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractPageWork;
import com.vonchange.jdbc.abstractjdbc.model.DataSourceWrapper;
import com.vonchange.jdbc.abstractjdbc.model.EntityCu;
import com.vonchange.jdbc.abstractjdbc.model.EntityInsertResult;
import com.vonchange.jdbc.abstractjdbc.model.EntityUpdateResult;
import com.vonchange.jdbc.abstractjdbc.template.MyJdbcTemplate;
import com.vonchange.jdbc.abstractjdbc.util.ConvertMap;
import com.vonchange.jdbc.abstractjdbc.util.markdown.MarkdownUtil;
import com.vonchange.jdbc.abstractjdbc.util.markdown.bean.SqlInfo;
import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.config.Constant;
import com.vonchange.mybatis.dialect.Dialect;
import com.vonchange.mybatis.tpl.EntityUtil;
import com.vonchange.mybatis.tpl.MybatisTpl;
import com.vonchange.mybatis.tpl.exception.MybatisMinRuntimeException;
import com.vonchange.mybatis.tpl.model.EntityField;
import com.vonchange.mybatis.tpl.model.EntityInfo;
import com.vonchange.mybatis.tpl.model.SqlWithParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * jdbc core
 * by von_change
 */
public abstract class AbstractJdbcCore implements JdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcCore.class);

    protected  abstract Dialect getDefaultDialect();

    protected abstract DataSourceWrapper getWriteDataSource();

    protected abstract boolean needInitEntityInfo();

    protected abstract boolean readAllScopeOpen();

    protected abstract int batchSize();


    protected abstract DataSourceWrapper getDataSourceFromSql(String sql);
    protected abstract boolean logRead();
    protected abstract boolean logWrite();
    protected abstract boolean logFullSql();
    protected abstract boolean needReadMdLastModified();

    private static volatile IJdbcBase jdbcBase = null;
    private static volatile Dialect dialect = null;
    private   Dialect getDialect(){
        if (null == dialect) {
            synchronized (Dialect.class) {
                if (null == dialect) {
                    dialect = getDefaultDialect();
                }
            }
        }
        return dialect;
    }

    private IJdbcBase getJdbcBase() {
        if (null == jdbcBase) {
            synchronized (JdbcBaseImpl.class) {
                if (null == jdbcBase) {
                    jdbcBase = new JdbcBaseImpl() {
                        @Override
                        protected MyJdbcTemplate initJdbcTemplate(DataSourceWrapper dataSourceWrapper, Constants.EnumRWType enumRWType, String sql) {
                            return getJdbcTemplate(dataSourceWrapper, enumRWType, sql);
                        }

                        @Override
                        protected boolean logReadSwitch() {
                            return logRead();
                        }

                        @Override
                        protected  boolean logWriteSwitch() {
                            return logWrite();
                        }

                        @Override
                        protected boolean logFullSqlSwitch() {
                            return logFullSql();
                        }

                    };
                }
            }
        }
        return jdbcBase;
    }


    private static Map<String, MyJdbcTemplate> yhJdbcTemplateMap = new ConcurrentHashMap<>();

    private DataSourceWrapper getDataSourceWrapper(DataSourceWrapper dataSourceWrapper, Constants.EnumRWType enumRWType,
                                                   String sql) {
        if (null != dataSourceWrapper) {
            return dataSourceWrapper;
        }
        //from sql get datasource
        DataSourceWrapper dataSourceFromSql = getDataSourceFromSql(sql);
        if (null != dataSourceFromSql) {
            return dataSourceFromSql;
        }
        //去除 直接读随机数据源 主从有延迟 还是需要根据业务指定数据源
        if (enumRWType.equals(Constants.EnumRWType.read) && readAllScopeOpen()) {
            return getReadDataSource();
        }
        return getWriteDataSource();
    }

    private MyJdbcTemplate getJdbcTemplate(DataSourceWrapper dataSourceWrapper, Constants.EnumRWType enumRWType, String sql) {
        DataSourceWrapper dataSource = getDataSourceWrapper(dataSourceWrapper, enumRWType, sql);
        log.debug("\n====== use dataSource key {}", dataSource.getKey());
        if (yhJdbcTemplateMap.containsKey(dataSource.getKey())) {
            return yhJdbcTemplateMap.get(dataSource.getKey());
        }
        MyJdbcTemplate myJdbcTemplate = new MyJdbcTemplate(dataSource.getDataSource());
        myJdbcTemplate.setFetchSizeBigData(getDialect().getBigDataFetchSize());
        myJdbcTemplate.setFetchSize(getDialect().getFetchSize());
        yhJdbcTemplateMap.put(dataSource.getKey(), myJdbcTemplate);
        return myJdbcTemplate;
    }

    public final <T> int insertBatch(List<T> entityList,int batchSize) {
        return insertBatch(null, entityList,batchSize);
    }

    public final <T> int insertBatch(DataSourceWrapper dataSourceWrapper, List<T> entityList,int batchSize) {
        return insertBatch(null, entityList, false,batchSize);
    }

    public final <T> int insertBatchDuplicateKey(List<T> entityList,int batchSize) {
        return insertBatchDuplicateKey(null, entityList,batchSize);
    }

    public final <T> int insertBatchDuplicateKey(DataSourceWrapper dataSourceWrapper, List<T> entityList,int batchSize) {
        return insertBatch(dataSourceWrapper, entityList, true,batchSize);
    }

    private <T> int insertBatch(DataSourceWrapper dataSourceWrapper, List<T> entityList, boolean duplicate,int batchSize) {
        if (null == entityList || entityList.isEmpty()) {
            return 0;
        }
        SqlWithParam sqlParmeter = generateInsertSql(entityList.get(0), duplicate);
        String sql = sqlParmeter.getSql();
        List<Object[]> list = new ArrayList<>();
        int i = 0;
        if(batchSize<=0){
            batchSize=batchSize();
        }
        List<List<Object[]>> listSplit = new ArrayList<>();
        for (T t : entityList) {
            if (i != 0 && i % batchSize == 0) {
                listSplit.add(list);
                list = new ArrayList<>();
            }
            SqlWithParam sqlParameterItem = generateInsertSql(t, duplicate);
            list.add(sqlParameterItem.getParams());
            i++;
        }
        if (!list.isEmpty()) {
            listSplit.add(list);
        }
        int num=0;
        for (List<Object[]> item : listSplit) {
            int[] result = getJdbcBase().updateBatch(dataSourceWrapper, sql, item);
            num+=result.length;
            log.debug("\ninsertBatch {}", result);
        }
        return num;
    }

    public final <T> int insert(DataSourceWrapper dataSourceWrapper, T entity) {
        SqlWithParam sqlParmeter = generateInsertSql(entity, false);
        return getJdbcBase().insert(dataSourceWrapper,entity, sqlParmeter.getSql(),sqlParmeter.getColumnReturns(), sqlParmeter.getParams());
    }

    public final <T> int insert(T entity) {
        return insert(null, entity);
    }


    public final <T> int update(DataSourceWrapper dataSourceWrapper, T entity) {
        SqlWithParam sqlParmeter = generateUpdateEntitySql(entity, false);
        return getJdbcBase().update(dataSourceWrapper, sqlParmeter.getSql(), sqlParmeter.getParams());
    }

    public final <T> int update(T entity) {
        return update(null, entity);
    }

    public final <T> int updateAllField(DataSourceWrapper dataSourceWrapper, T entity) {
        SqlWithParam sqlParmeter = generateUpdateEntitySql(entity, true);
        return getJdbcBase().update(dataSourceWrapper, sqlParmeter.getSql(), sqlParmeter.getParams());
    }

    public final <T> int updateAllField(T entity) {
        return updateAllField(null, entity);
    }

    public final <T> int insertDuplicateKey(T entity) {
        return insertDuplicateKey(null, entity);
    }

    public final <T> int insertDuplicateKey(DataSourceWrapper dataSourceWrapper, T entity) {
        SqlWithParam sqlParmeter = generateInsertSql(entity, true);
        return getJdbcBase().insert(dataSourceWrapper, entity,sqlParmeter.getSql(),sqlParmeter.getColumnReturns(), sqlParmeter.getParams());
    }

    private void initEntityInfo(Class<?> clazz) {
        if (needInitEntityInfo()) {
            EntityUtil.initEntityInfo(clazz);
        }
    }

    private static Object getPublicPro(Object bean, String name) {
        if (name.equals("serialVersionUID")) {//??????
            return null;
        }
        return Constant.BeanUtil.getProperty(bean, name);
    }

    //private String
    private <T> SqlWithParam generateInsertSql(T entity, boolean duplicate) {
        initEntityInfo(entity.getClass());
        SqlWithParam sqlParmeter = new SqlWithParam();
        List<EntityCu> entityCuArrayList = new ArrayList<>();
        EntityInfo entityInfo = EntityUtil.getEntityInfo(entity.getClass());
        String tableName = entityInfo.getTableName();
        Map<String, EntityField> entityFieldMap = entityInfo.getFieldMap();
        for (Map.Entry<String, EntityField> entry : entityFieldMap.entrySet()) {
            String fieldName = entry.getKey();
            EntityField entityField = entry.getValue();
            if (Boolean.TRUE.equals(entityField.getIsColumn())) {
                Object value = getPublicPro(entity, fieldName);
                entityCuArrayList.add(new EntityCu(entityField, value, duplicate, false));
            }
        }
        workInsertItem(entityCuArrayList);
        EntityInsertResult entityInsertResult = getInsertValueSql(entityCuArrayList);
        String insertSql = StringUtils.format("insert into {0}({1}) values ({2})", tableName, entityInsertResult.getKeySql(), entityInsertResult.getValueSql());
        if (duplicate) {
            insertSql = insertSql + " ON DUPLICATE KEY UPDATE " + entityInsertResult.getUpdateStr();
        }
        sqlParmeter.setColumnReturns(entityInfo.getColumnReturns());
        sqlParmeter.setSql(insertSql);
        sqlParmeter.setParams(entityInsertResult.getValueList().toArray());
        return sqlParmeter;
    }

    private String updateIfNullValue(EntityCu entityCu, boolean nullUpdate) {
        EntityField entityField = entityCu.getEntityField();
        entityCu.setUpdateValueColumn(true);
        entityCu.setUpdateValueParam(true);
        if (StringUtils.isNotBlank(entityField.getUpdateIfNullFunc())) {
            entityCu.setUpdateValueParam(false);
            return entityField.getUpdateIfNullFunc();
        }
        if (StringUtils.isNotBlank(entityField.getUpdateIfNull())) {
            entityCu.setUpdateValueParam(false);
            return "'" + entityField.getUpdateIfNull() + "'";
        }
        if (nullUpdate && Boolean.FALSE.equals(entityField.getUpdateNotNull())) {
            return "?";
        }
        if (null != entityCu.getValue()) {
            return "?";
        }
        entityCu.setUpdateValueParam(false);
        entityCu.setUpdateValueColumn(false);
        return null;
    }

    private String insertIfNullValue(EntityCu entityCu) {
        EntityField entityField = entityCu.getEntityField();
        entityCu.setInsertKeyColumn(true);
        entityCu.setInsertValueParam(true);
        if (StringUtils.isNotBlank(entityField.getInsertIfNullFunc())) {
            entityCu.setInsertValueParam(false);
            return entityField.getInsertIfNullFunc();
        }
        if (StringUtils.isNotBlank(entityField.getInsertIfNull())) {
            entityCu.setInsertValueParam(false);
            return "'" + entityField.getInsertIfNull() + "'";
        }
        if (null != entityCu.getValue()) {
            return "?";
        }
        entityCu.setInsertValueParam(false);
        entityCu.setInsertKeyColumn(false);
        return null;
    }

    private void workInsertItem(List<EntityCu> entityCuList) {
        for (EntityCu entityCu : entityCuList) {
            entityCu.setInsertIfNullValue(insertIfNullValue(entityCu));
            if (Boolean.TRUE.equals(entityCu.getDuplicate())) {
                entityCu.setUpdateIfNullValue(updateIfNullValue(entityCu, false));
            }
        }
    }

    private EntityInsertResult getInsertValueSql(List<EntityCu> entityCus) {
        StringBuilder valueStr = new StringBuilder();
        StringBuilder keyStr = new StringBuilder();
        StringBuilder updateStr = new StringBuilder(" ");
        List<Object> valueList = new ArrayList<>();
        List<Object> valueUpdateList = new ArrayList<>();
        for (EntityCu entityCu : entityCus) {
            if (Boolean.TRUE.equals(entityCu.getInsertKeyColumn())) {
                valueStr.append(entityCu.getInsertIfNullValue()).append(",");
                keyStr.append("`").append(entityCu.getEntityField().getColumnName()).append("`").append(",");
            }
            if (Boolean.TRUE.equals(entityCu.getInsertValueParam())) {
                valueList.add(entityCu.getValue());
            }
            if (Boolean.TRUE.equals(entityCu.getDuplicate()) && Boolean.TRUE.equals(entityCu.getUpdateValueColumn()) && Boolean.FALSE.equals(entityCu.getEntityField().getIgnoreDupUpdate())) {
                updateStr.append("`").append(entityCu.getEntityField().getColumnName()).append("`")
                        .append("=").append( entityCu.getUpdateIfNullValue())
                        .append(",");
                if (Boolean.TRUE.equals(entityCu.getUpdateValueParam())) {
                    valueUpdateList.add(entityCu.getValue());
                }
            }
        }
        valueList.addAll(valueUpdateList);
        return new EntityInsertResult(keyStr.substring(0, keyStr.length() - 1),
                valueStr.substring(0, valueStr.length() - 1), updateStr.substring(0, updateStr.length() - 1), valueList);
    }

    private <T> SqlWithParam generateUpdateEntitySql(T entity, boolean isNullUpdate) {
        SqlWithParam sqlParmeter = new SqlWithParam();
        initEntityInfo(entity.getClass());
        EntityInfo entityInfo = EntityUtil.getEntityInfo(entity.getClass());
        String tableName = entityInfo.getTableName();
        String idColumnName = entityInfo.getIdColumnName();
        if(null==idColumnName){
            throw  new MybatisMinRuntimeException("need entity field @ID");
        }
        Object idValue = getPublicPro(entity, entityInfo.getIdFieldName());
        if (null == idValue) {
            throw new MybatisMinRuntimeException("ID value is null,can not update");
        }
        List<EntityCu> entityCuList = new ArrayList<>();
        Map<String, EntityField> entityFieldMap = entityInfo.getFieldMap();
        for (Map.Entry<String, EntityField> entry : entityFieldMap.entrySet()) {
            EntityField entityField = entry.getValue();
            if (Boolean.TRUE.equals(entityField.getIsColumn()) && Boolean.FALSE.equals(entityField.getIsId())) {
                Object value = getPublicPro(entity, entry.getKey());
                entityCuList.add(new EntityCu(entityField, value, false, isNullUpdate));
            }
        }
        workUpdateItem(entityCuList);
        EntityUpdateResult entityUpdateResult = workUpdateSql(entityCuList);
        List<Object> valueList = entityUpdateResult.getValueList();
        valueList.add(idValue);
        //0:tableName 1:setSql 2:idName
        String sql = StringUtils.format("update {0} set {1} where {2}=?", tableName, entityUpdateResult.getUpdateStr(), idColumnName);
        sqlParmeter.setSql(sql);
        sqlParmeter.setParams(valueList.toArray());
        return sqlParmeter;
    }

    private void workUpdateItem(List<EntityCu> entityCuList) {
        for (EntityCu entityCu : entityCuList) {
            entityCu.setUpdateIfNullValue(updateIfNullValue(entityCu, entityCu.getNullUpdate()));
        }
    }

    private EntityUpdateResult workUpdateSql(List<EntityCu> entityCus) {
        String item = "{0}";
        StringBuilder updateStr = new StringBuilder();
        List<Object> valueList = new ArrayList<>();
        for (EntityCu entityCu : entityCus) {
            if (Boolean.TRUE.equals(entityCu.getUpdateValueColumn())) {
                updateStr.append("`").append(entityCu.getEntityField().getColumnName()).append("`").append("=")
                        .append(StringUtils.format(item, entityCu.getUpdateIfNullValue())).append(",");
            }
            if (Boolean.TRUE.equals(entityCu.getUpdateValueParam())) {
                valueList.add(entityCu.getValue());
            }
        }
        return new EntityUpdateResult(updateStr.substring(0, updateStr.length() - 1), valueList);
    }

    //crud end
    public final <T> T queryById(DataSourceWrapper dataSourceWrapper, Class<T> type, Object id) {
        String sql = generateQueryByIdSql(type);
        return getJdbcBase().queryOne(dataSourceWrapper, type, sql, id);
    }

    public final <T> T queryById(Class<T> type, Object id) {
        return queryById(null, type, id);
    }

    private String generateQueryByIdSql(Class<?> type) {
        initEntityInfo(type);
        EntityInfo entityInfo = EntityUtil.getEntityInfo(type);
        Map<String, EntityField> fieldMap = entityInfo.getFieldMap();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,EntityField> entry:fieldMap.entrySet()) {
            if(entry.getValue().getIsColumn()){
                sb.append(entry.getValue().getColumnName()).append(",");
            }
        }
        String tableName = entityInfo.getTableName();
        String idName = entityInfo.getIdColumnName();
        return StringUtils.format("select {0} from {1} where  {2} = ?", sb.substring(0,sb.length()-1),tableName, idName);
    }

    public final <T> List<T> queryList(DataSourceWrapper dataSourceWrapper, Class<T> type, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId,parameter);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlinfo.getSql(), parameter);
        return getJdbcBase().queryList(dataSourceWrapper, type, sqlParmeter.getSql(), sqlParmeter.getParams());
    }

    public final <T> List<T> queryList(Class<T> type, String sqlId, Map<String, Object> parameter) {
        return queryList(null, type, sqlId, parameter);
    }


    public <T> T queryOne(DataSourceWrapper dataSourceWrapper, Class<T> type, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId,parameter);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlInfo.getSql(), parameter);
        List<T> list= getJdbcBase().queryList(dataSourceWrapper, type, sqlParmeter.getSql(), sqlParmeter.getParams());
        if(list.isEmpty()){
            return null;
        }
        if(list.size()>1){
            log.warn("{} expect one row but found {} rows",sqlId,list.size());
        }
        return list.get(0);
    }

    public <T> T queryOne(Class<T> type, String sqlId, Map<String, Object> parameter) {
        return queryOne(null, type, sqlId, parameter);
    }


    public Map<String, Object> queryMapOne(DataSourceWrapper dataSourceWrapper, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId,parameter);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlinfo.getSql(), parameter);
        List<Map<String, Object>> list= getJdbcBase().queryListResultMap(dataSourceWrapper, sqlParmeter.getSql(), sqlParmeter.getParams());
        if(list.isEmpty()){
            return null;
        }
        if(list.size()>1){
            log.warn("{} expect one row but found {} rows",sqlId,list.size());
        }
        return list.get(0);
    }

    public Map<String, Object> queryMapOne(String sqlId, Map<String, Object> parameter) {
        return queryMapOne(null, sqlId, parameter);
    }

    public final List<Map<String, Object>> queryMapList(DataSourceWrapper dataSourceWrapper, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId,parameter);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlInfo.getSql(), parameter);
        return getJdbcBase().queryListResultMap(dataSourceWrapper, sqlParmeter.getSql(), sqlParmeter.getParams());
    }

    public final List<Map<String, Object>> queryMapList(String sqlId, Map<String, Object> parameter) {
        return queryMapList(null, sqlId, parameter);
    }


    public final void queryMapBigData(String sqlId, AbstractMapPageWork pageWork, Map<String, Object> parameter) {
         queryMapBigData(null, sqlId, pageWork, parameter);
    }

    public final void queryMapBigData(DataSourceWrapper dataSourceWrapper, String sqlId, AbstractMapPageWork pageWork, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId,parameter);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlInfo.getSql(), parameter);
         getJdbcBase().queryForBigData(dataSourceWrapper, sqlParmeter.getSql(), pageWork, sqlParmeter.getParams());
    }

    public final <T> void queryBigData(Class<T> type, String sqlId, AbstractPageWork pageWork, Map<String, Object> parameter) {
          queryBigData(null, type, sqlId, pageWork, parameter);
    }
    @SuppressWarnings("unchecked")
    public final <T> void queryBigData(DataSourceWrapper dataSourceWrapper, Class<T> type, String sqlId, AbstractPageWork pageWork, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId,parameter);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlInfo.getSql(), parameter);
         getJdbcBase().queryForBigData(dataSourceWrapper, type, sqlParmeter.getSql(), pageWork, sqlParmeter.getParams());
    }

    public final Page<Map<String, Object>> queryMapPage(String sqlId, Pageable pageable, Map<String, Object> parameter) {
        return queryMapPage(null, sqlId, pageable, parameter);
    }

    public final Page<Map<String, Object>> queryMapPage(DataSourceWrapper dataSourceWrapper, String sqlId, Pageable pageable, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId,parameter);
        SqlInfo countSqlInfo = getSqlInfo(sqlId + ConstantJdbc.COUNTFLAG,parameter);
        String countSql = countSqlInfo.getSql();
        boolean hasCountSqlInMd = true;
        if (StringUtils.isBlank(countSql)) {
            countSql = null;
            hasCountSqlInMd = false;
        }
        SqlWithParam sqlParmeter = getSqlParmeter(sqlInfo.getSql(), parameter);
        long totalCount = countMySqlResult(dataSourceWrapper, sqlInfo.getSql(), countSql, parameter);
        String sql = sqlParmeter.getSql();
        int pageNum = pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber();
        int firstEntityIndex = pageable.getPageSize() * pageNum;
        boolean hasLimit = false;
        if (hasCountSqlInMd) {
            hasLimit = hasLimit(sql);
        }
        if (!hasCountSqlInMd || !hasLimit) {
            sql = getDialect().getPageSql(sql, firstEntityIndex, pageable.getPageSize());
        }
        List<Map<String, Object>> entities = getJdbcBase().queryListResultMap(dataSourceWrapper, sql, sqlParmeter.getParams());
        return new PageImpl<>(entities, pageable, totalCount);
    }

    public <T> Page<T> queryPage(Class<T> type, String sqlId, Pageable pageable, Map<String, Object> parameter) {
        return queryPage(null, type, sqlId, pageable, parameter);
    }

    public <T> Page<T> queryPage(DataSourceWrapper dataSourceWrapper, Class<T> type, String sqlId, Pageable pageable, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId,parameter);
        SqlInfo countSqlInfo = getSqlInfo(sqlId + ConstantJdbc.COUNTFLAG,parameter);
        String countSql = countSqlInfo.getSql();
        boolean hasCountSqlInMd = true;
        if (StringUtils.isBlank(countSqlInfo.getSql())) {
            countSql = null;
            hasCountSqlInMd = false;
        }
        SqlWithParam sqlParmeter = getSqlParmeter(sqlinfo.getSql(), parameter);
        long totalCount = countMySqlResult(dataSourceWrapper, sqlinfo.getSql(), countSql, parameter);
        String sql = sqlParmeter.getSql();
        int pageNum = pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber();
        int firstEntityIndex = pageable.getPageSize() * pageNum;
        boolean hasLimit = false;
        if (hasCountSqlInMd) {
            hasLimit = hasLimit(sql);
        }
        if (!hasCountSqlInMd || !hasLimit) {
            sql = getDialect().getPageSql(sql, firstEntityIndex, pageable.getPageSize());
        }
        List<T> entities = getJdbcBase().queryList(dataSourceWrapper, type, sql, sqlParmeter.getParams());
        return new PageImpl<>(entities, pageable, totalCount);
    }

    private boolean hasLimit(String sql) {
        String lowerSql = sql.toLowerCase();
        if (lowerSql.contains("limit ") || lowerSql.contains("limit\n")) {
            return true;
        }
        return false;
    }

    private long countMySqlResult(DataSourceWrapper dataSourceWrapper, String sql, String countSql, Map<String, Object> params) {
        if (params.containsKey(ConstantJdbc.PageParam.COUNT)) {
            countSql = ConvertUtil.toString(params.get(ConstantJdbc.PageParam.COUNT));
        }
        //count num 　_count = @123
        if (!StringUtils.isBlank(countSql) && countSql.startsWith(ConstantJdbc.PageParam.AT)) {
            return ConvertUtil.toLong(countSql.substring(1));
        }
        Object result = null;

        if (!StringUtils.isBlank(countSql)) {
            result = findBy(dataSourceWrapper, countSql, params);
        }
        if (StringUtils.isBlank(countSql)) {
            SqlWithParam sqlParmeter = getSqlParmeter(sql, params);
            countSql = generateMyCountSql(sqlParmeter.getSql());
            result = getJdbcBase().queryOneColumn(dataSourceWrapper, countSql, 1, sqlParmeter.getParams());
        }
        if (null == result) {
            return 0L;
        }
        return ConvertUtil.toLong(result);
    }


    public <T> T queryOneColumn(Class<?> targetType, String sqlId, Map<String, Object> parameter) {
        return queryOneColumn(null, targetType, sqlId, parameter);
    }

    public <T> T queryOneColumn(DataSourceWrapper dataSourceWrapper, Class<?> targetType, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId,parameter);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlInfo.getSql(), parameter);
        Object result = getJdbcBase().queryOneColumn(dataSourceWrapper, sqlParmeter.getSql(), 1, sqlParmeter.getParams());
        return ConvertUtil.toObject(result, targetType);
    }

    private Object findBy(DataSourceWrapper dataSourceWrapper, String sql, Map<String, Object> parameter) {
        SqlWithParam sqlParmeter = getSqlParmeter(sql, parameter);
        return getJdbcBase().queryOneColumn(dataSourceWrapper, sqlParmeter.getSql(), 1, sqlParmeter.getParams());
    }

    private SqlWithParam getSqlParmeter(String sql, Map<String, Object> parameter) {
        return MybatisTpl.generate(sql,parameter,getDialect());
    }

    public <T> Map<String, T> queryToMap(Class<T> c, String sqlId, String keyInMap, Map<String, Object> parameter) {
        return queryToMap(null, c, sqlId, keyInMap, parameter);
    }

    public <T> Map<String, T> queryToMap(DataSourceWrapper dataSourceWrapper, Class<T> c, String sqlId, String keyInMap, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId,parameter);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlInfo.getSql(), parameter);
        return getJdbcBase().queryMapList(dataSourceWrapper, c, sqlParmeter.getSql(), keyInMap, sqlParmeter.getParams());
    }

    private SqlInfo getSqlInfo(String sqlId,Map<String,Object> parameter) {
        if(null==parameter){
            parameter= new LinkedHashMap<>();
        }
        parameter.put(MybatisTpl.MARKDOWN_SQL_ID,sqlId);
        return MarkdownUtil.getSqlInfo(sqlId,needReadMdLastModified());
    }

    private String generateMyCountSql(String sql) {
        StringBuilder sb = new StringBuilder();
        Matcher m = Pattern.compile("(/\\*)([\\w\\s\\@\\:]*)(\\*/)").matcher(sql);
        while (m.find()) {
            String group = m.group();
            sb.append(group);
        }
        CountSqlParser countSqlParser = new CountSqlParser();
        return sb.toString() + countSqlParser.getSmartCountSql(sql);
    }
    public <T> int batchUpdate( String sqlId, List<T> entityList) {
        return batchUpdate(null,sqlId, entityList,0);
    }
    public <T> int batchUpdate( String sqlId, List<T> entityList,int size) {
        return batchUpdate(null,sqlId, entityList,size);
    }
    public <T> int batchUpdate(DataSourceWrapper dataSourceWrapper, String sqlId, List<T> entityList){
        return batchUpdate(dataSourceWrapper, sqlId, entityList, 0);
    }
    public <T> int batchUpdate(DataSourceWrapper dataSourceWrapper, String sqlId, List<T> entityList,int size) {
        if (null == entityList || entityList.isEmpty()) {
            return 0;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        try {
            map = ConvertMap.toMap(entityList.get(0).getClass());
        } catch (IntrospectionException e) {
            log.error("IntrospectionException ", e);
        }
        SqlInfo sqlinfo = getSqlInfo(sqlId,map);
        SqlWithParam sqlParmeter = getSqlParmeter(sqlinfo.getSql(), map);
        String sql = sqlParmeter.getSql();
        List<Object[]> param = new ArrayList<>();
        int i = 0;
        List<List<Object[]>> listSplit = new ArrayList<>();
        int batchSize =size;
        if(size<=0){
            batchSize = batchSize();
        }
        for (T t : entityList) {
            if (i != 0 && i % batchSize == 0) {
                listSplit.add(param);
                param = new ArrayList<>();
            }
            param.add(beanToObjects(t, sqlParmeter.getPropertyNames()));
            i++;
        }
        if (!param.isEmpty()) {
            listSplit.add(param);
        }
        int updateNum=0;
        for (List<Object[]> item : listSplit) {
            int[] result = getJdbcBase().updateBatch(dataSourceWrapper, sql, item);
            log.debug("\nbatchUpdateBySql {}", result);
            updateNum+=result.length;
        }
        return updateNum;
    }

    private <T> Object[] beanToObjects(T t, List<String> propertyNames) {
        List<Object> result = new ArrayList<>();
        for (String propertyName: propertyNames) {
            result.add(Constant.BeanUtil.getProperty(t, propertyName));
        }
        return result.toArray();
    }

    public int update(String sqlId, Map<String, Object> parameter) {
        return update(null, sqlId, parameter);
    }

    public int update(DataSourceWrapper dataSourceWrapper, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId,parameter);
        String sql = sqlinfo.getSql();
        SqlWithParam sqlParmeter = getSqlParmeter(sql, parameter);
        return getJdbcBase().update(dataSourceWrapper, sqlParmeter.getSql(), sqlParmeter.getParams());
    }

    public int insert(String sqlId, Map<String, Object> parameter) {
        return insert(null, sqlId, parameter);
    }

    public int insert(DataSourceWrapper dataSourceWrapper, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId,parameter);
        String sql = sqlinfo.getSql();
        SqlWithParam sqlParmeter = getSqlParmeter(sql, parameter);
        return getJdbcBase().insert(dataSourceWrapper, sqlParmeter.getSql(), sqlParmeter.getParams());
    }

}
