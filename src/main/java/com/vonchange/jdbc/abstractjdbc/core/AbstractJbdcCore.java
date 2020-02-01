package com.vonchange.jdbc.abstractjdbc.core;


import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.jdbc.abstractjdbc.dialect.Dialect;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractMapPageWork;
import com.vonchange.jdbc.abstractjdbc.handler.AbstractPageWork;
import com.vonchange.jdbc.abstractjdbc.model.SqlParmeter;
import com.vonchange.jdbc.abstractjdbc.parser.CountSqlParser;
import com.vonchange.jdbc.abstractjdbc.template.YhJdbcTemplate;
import com.vonchange.jdbc.abstractjdbc.util.SqlUtil;
import com.vonchange.jdbc.abstractjdbc.util.mardown.MarkdownUtil;
import com.vonchange.jdbc.abstractjdbc.util.mardown.bean.SqlInfo;
import com.vonchange.jdbc.abstractjdbc.util.sql.AbstractSqlDialectUtil;
import com.vonchange.jdbc.abstractjdbc.util.sql.SqlFill;
import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.tpl.exception.MyRuntimeException;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 冯昌义
 * @brief
 * @details
 * @date 2018/1/9.
 */
public abstract class AbstractJbdcCore extends AbstractJdbcCud {
    private static final Logger logger = LoggerFactory.getLogger(AbstractJbdcCore.class);


    /*去掉cache实现  private ICache<Object> jdbcCache;*/

    //private static ExecutorService threadPool = Executors.newFixedThreadPool(ConvertUtil.toInteger(SpringPropertiesUtil.getValue(ConstantJdbc.THREADNUMNAME, ConstantJdbc.THREADNUM)));

    protected abstract String getDataSource();

    protected abstract <T> List<T> queryList(Class<T> type, String sql, Object... args);

    protected abstract List<Map<String, Object>> queryListResultMap(String sql, Object... args);

    protected abstract Page<Map<String, Object>> queryForBigData(String sql, AbstractMapPageWork pageWork, Object... args);

    protected abstract <T> Page<T> queryForBigData(Class<T> type, String sql, AbstractPageWork<T> pageWork, Object... args);

    protected abstract <T> T queryOne(Class<T> type, String sql, Object... args);

    protected abstract Map<String, Object> queryUniqueResultMap(String sql, Object... args);

    protected abstract Object queryOneColumn(String sql, int columnIndex, Object... args);

    protected abstract DataSource getDataSource(String dataSourceName);

    protected abstract <T> Class<T> getClazz();

    protected abstract <T> Map<String, T> queryMapList(Class<T> c, String sql, String keyInMap, Object... args);
    private YhJdbcTemplate getJdbcTemplateInSpring(String dataSource) {
        if (StringUtils.isBlank(dataSource)) {
            dataSource = ConstantJdbc.DataSource.DEFAULT;
        }
        DataSource dataSourceDs = getDataSource(dataSource);
                //SpringUtil.getBean(dataSource);
        return new YhJdbcTemplate(dataSourceDs);
    }

    public YhJdbcTemplate getJdbcTemplate(String sql) {
        String dataSource = getAbstractSqlDialectUtil().getDataSource(sql);
        YhJdbcTemplate yhJdbcTemplate = getJdbcTemplateInSpring(dataSource);
        yhJdbcTemplate.setFetchSizeBigData(getDialect(sql).getBigDataFetchSize());
        yhJdbcTemplate.setFetchSize(getDialect(sql).getFetchSize());
        return yhJdbcTemplate;
    }
    private AbstractSqlDialectUtil getAbstractSqlDialectUtil() {
        Dialect dialect = getDefaultDialect();
        String dataSource = getDataSource();
        AbstractSqlDialectUtil abstractSqlDialectUtil = new AbstractSqlDialectUtil() {
            @Override
            protected Dialect getDefaultDialect() {
                return dialect;
            }

            @Override
            protected String getDataSource() {
                return dataSource;
            }
        };
        return abstractSqlDialectUtil;
    }

    protected Dialect getDialect(String sql) {
        return getAbstractSqlDialectUtil().getDialect(sql);
    }

    private String getSqlIdByClazz(String sqlId) {
        if(sqlId.startsWith(ConstantJdbc.ISMDFLAG)){
            return  sqlId;
        }
        if(sqlId.startsWith(ConstantJdbc.ISSQLFLAG)){
            return  sqlId;
        }
        if(sqlId.contains(".")){
            return sqlId;
        }
        if (null == this.getClazz()) {
            throw new MyRuntimeException("请实现getClazz");
        }
        String name = this.getClazz().getSimpleName();
        // @TODO 优先sql 下 其次是 同类包下
        return StringUtils.format("{0}.{1}.{2}", ConstantJdbc.SQLFATH, name, sqlId);
    }

    public <T> List<T> queryListBySqlId(String sqlId, Map<String, Object> parameter) {
        return findListBySqlId(this.getClazz(), this.getSqlIdByClazz(sqlId), parameter);
    }


    public <T> List<T> findListBySqlId(Class<T> type, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        return findList(type, sqlinfo.getSql(), parameter);
    }


    public <T> List<T> findList(Class<T> type, String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findList(type, sqlParmeter.getSql(), sqlParmeter.getParameters());
    }


    public <T> List<T> findList(Class<T> type, String sql, Object... args) {
        return queryList(type, sql, args);
    }

    public <T> T queryBeanBySqlId(String sqlId, Map<String, Object> parameter) {
        return findBeanBySqlId(this.getClazz(), this.getSqlIdByClazz(sqlId), parameter);
    }

    public <T> T findBeanBySqlId(Class<T> type, String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        return findBean(type, sqlinfo.getSql(), parameter);
    }

    public <T> Page<T> queryPageBySqlId(String sqlId, Pageable pageable, Map<String, Object> parameter) {
        return findPageBySqlId(this.getClazz(), this.getSqlIdByClazz(sqlId), pageable, parameter);
    }

    public <T> Page<T> findPageBySqlId(Class<T> type, String sqlId, Pageable pageable, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        SqlInfo countSqlInfo = getSqlInfo(sqlId + ConstantJdbc.COUNTFLAG);
        String countSql = countSqlInfo.getSql();
        if (StringUtils.isBlank(countSqlInfo.getSql())) {
            countSql = null;
        }
        return findPage(type, sqlinfo.getSql(), countSql, pageable, parameter);
    }


    public <T> T findBean(Class<T> type, String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findBean(type, sqlParmeter.getSql(), sqlParmeter.getParameters());
    }


    public <T> T findBean(Class<T> type, String sql, Object... args) {
        sql = removeLimit(sql);
        sql = getDialect(sql).getLimitOne(sql);
        return queryOne(type, sql, args);
    }


    public List<Map<String, Object>> findList(String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findList(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }

    public Map<String, Object> queryOneBySqlId(String sqlId, Map<String, Object> parameter) {
        return findOneBySqlId(this.getSqlIdByClazz(sqlId), parameter);
    }

    public Map<String, Object> findOneBySqlId(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        return findOne(sqlinfo.getSql(), parameter);
    }

    public Map<String, Object> findOne(String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findOne(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }


    public Map<String, Object> findOne(String sql, Object... args) {
        sql = removeLimit(sql);
        sql = getDialect(sql).getLimitOne(sql);
        return queryUniqueResultMap(sql, args);
    }

    public List<Map<String, Object>> findList(String sql, Object... args) {
        return queryListResultMap(sql, args);
    }


    public final Page<Map<String, Object>> findPageBySqlIdBigData(String sqlId, AbstractMapPageWork pageWork, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return findBigData(sqlInfo.getSql(), pageWork, parameter);
    }

    public Page<Map<String, Object>> findBigData(String sql, AbstractMapPageWork pageWork, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findBigData(sqlParmeter.getSql(), pageWork, sqlParmeter.getParameters());
    }

    public Page<Map<String, Object>> findBigData(String sql, AbstractMapPageWork pageWork, Object... args) {
        return queryForBigData(sql, pageWork, args);
    }

    public final <T> Page<T> findPageBySqlIdBigData(Class<T> type, String sqlId, AbstractPageWork pageWork, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return findBigData(type, sqlInfo.getSql(), pageWork, parameter);
    }

    public <T> Page<T> findBigData(Class<T> type, String sql, AbstractPageWork pageWork, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findBigData(type, sqlParmeter.getSql(), pageWork, sqlParmeter.getParameters());
    }

    public <T> Page<T> findBigData(Class<T> type, String sql, AbstractPageWork pageWork, Object... args) {
        return queryForBigData(type, sql, pageWork, args);
    }




    public final List<Map<String, Object>> findListBySqlId(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return findList(sqlInfo.getSql(), parameter);
    }


    public final Page<Map<String, Object>> findPageBySqlId(String sqlId, Pageable pageable, Map<String, Object> parameter) {
        sqlId=getSqlIdByClazz(sqlId);
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        SqlInfo countSqlInfo = getSqlInfo(sqlId + ConstantJdbc.COUNTFLAG);
        String countSql = countSqlInfo.getSql();
        if (StringUtils.isBlank(countSql)) {
            countSql = null;
        }
        return findPage(sqlInfo.getSql(), countSql, pageable, parameter);
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
        Integer firstEntityIndex = pageable.getPageSize() * pageNum;
        sql = getDialect(sql).getPageSql(sql, firstEntityIndex, pageable.getPageSize());
        List<T> entities = findList(type, sql, parameter);
        return new PageImpl<>(entities, pageable, totalCount);
    }

    private Page<Map<String, Object>> findPage(String sql, long totalCount, Pageable pageable, Object... parameter) {
      /*  sql = removeLimit(sql);
        long totalCount = countMySqlResult(sql, countSql, parameter);*/
        int pageNum = pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber();
        Integer firstEntityIndex = pageable.getPageSize() * pageNum;
        sql = getDialect(sql).getPageSql(sql, firstEntityIndex, pageable.getPageSize());
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


    public Object findBySqlId(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return findBy(sqlInfo.getSql(), parameter);
    }

    public Object findBy(String sql, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return findBy(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }

    public Object findBy(String sql, Object... params) {
        return findBy(sql, 1, params);
    }

    private Object findBy(String sql, int columnIndex, Object... params) {
        return queryOneColumn(sql, columnIndex, params);
    }

    private SqlParmeter getSqlParmeter(String sql, Map<String, Object> parameter) {
        return  SqlUtil.getSqlParmeter(sql, parameter);
    }


    public <T> Map<String, T> queryBySqlId(Class<T> c, String sqlId, String keyInMap, Map<String, Object> parameter) {
        SqlInfo sqlInfo = getSqlInfo(sqlId);
        return queryBySql(c, sqlInfo.getSql(), keyInMap, parameter);
    }

    public <T> Map<String, T> queryBySql(Class<T> c, String sql, String keyInMap, Map<String, Object> parameter) {
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        Map<String, T> result = queryMapList(c, sqlParmeter.getSql(), keyInMap, sqlParmeter.getParameters());
        return result;
    }

    private SqlInfo getSqlInfo(String sqlId) {
    /*    if(sqlId.startsWith(ConstantJdbc.ISSQLFLAG)){
            SqlInfo sqlInfo=new SqlInfo();
            sqlInfo.setSql(sqlId.substring(ConstantJdbc.ISSQLFLAG.length()));
            return  sqlInfo;
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

    public final <T> T save(T entity) {
        return saveEntity(entity);
    }

    public final <T> T saveDuplicateKey(T entity) {
        return saveDuplicateKeyEntity(entity);
    }

    public final <T> int update(T entity) {
        return updateEntity(entity);
    }

    public final <T> int update(T entity, String... nullFields) {
        return updateEntity(entity, nullFields);
    }

    private List<List<String>> handerTables(String table) {
        if (StringUtil.isBlank(table)) {
            return new ArrayList<>();
        }
        String[] tables = table.split(",");
        Map<Integer, List<String>> map = new HashMap<>();
        String[] tableSubx;
        for (String tableSub : tables) {
            if (!tableSub.contains(":")) {
                tableSub = tableSub + ":0";
            }
            tableSubx = tableSub.split(":");
            Integer key = ConvertUtil.toInteger(tableSubx[1]);
            List<String> in = map.get(key);
            if (null == in) {
                in = new ArrayList<>();
            }
            in.add(tableSubx[0]);
            map.put(key, in);
        }
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < map.size(); i++) {
            result.add(map.get(i));
        }
        return result;
    }



    private void logSql(String sql, Object... params) {
        logger.info("\n原始sql为:\n{}\n参数为:{}\n生成的sql为:\n{}", sql, params, SqlFill.fill(sql, params));
    }


    public int updateBySqlId(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        String sql = sqlinfo.getSql();
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return update(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }

    public Object insertBySqlId(String sqlId, Map<String, Object> parameter) {
        SqlInfo sqlinfo = getSqlInfo(sqlId);
        String sql = sqlinfo.getSql();
        SqlParmeter sqlParmeter = getSqlParmeter(sql, parameter);
        return insert(sqlParmeter.getSql(), sqlParmeter.getParameters());
    }

}
