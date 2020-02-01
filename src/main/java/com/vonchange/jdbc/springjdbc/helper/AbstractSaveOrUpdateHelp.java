package com.vonchange.jdbc.springjdbc.helper;

import com.vonchange.jdbc.springjdbc.repository.JdbcRepository;
import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.common.util.map.MyHashMap;
import jodd.bean.BeanUtil;
import jodd.bean.BeanUtilBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by 冯昌义 on 2018/5/4.
 */
public abstract class AbstractSaveOrUpdateHelp<T> {

    protected  abstract JdbcRepository getJdbcRepository();
    protected  abstract  void saveBefore(T bean);
    protected  abstract  void updateBefore(T bean);
    protected  abstract  String getIdName();
    public    int  saveOrUpdate(Class<T> type, String sqlId, List<T> list, String[] keyList){
        String idName=getIdName();
        if(StringUtils.isBlank(idName)){
            idName="id";
        }
        int result=0;
        Map<String,Object> hasMap=new HashMap<>();
        List<T> findList=getJdbcRepository().findListBySqlId(type,sqlId,
                new MyHashMap().set("list",list));
        BeanUtil beanUtil = new BeanUtilBean();
        StringBuilder sb;
        Object idValue;
        for (T bean: findList) {
            idValue=beanUtil.getProperty(bean,idName);
            if(null==idValue){
                throw  new RuntimeException("请查询主键id" );
            }
            sb= new StringBuilder();
            for (String key: keyList) {
                sb.append(ConvertUtil.toString(beanUtil.getProperty(bean,key)));
            }
            hasMap.put(sb.toString(),idValue);
        }
        for (T bean: list) {
            sb= new StringBuilder();
            for (String key: keyList) {
                sb.append(ConvertUtil.toString(beanUtil.getProperty(bean,key)));
            }
            if(null==hasMap.get(sb.toString())){
                saveBefore(bean);
                try{
                    getJdbcRepository().save(bean);
                }catch (Exception e){
                    e.printStackTrace();
                }

                result++;
            }else{
                beanUtil.setProperty(bean,idName,hasMap.get(sb.toString()));
                updateBefore(bean);
                getJdbcRepository().update(bean);
                result++;
            }
        }
        return  result;
    }
}
