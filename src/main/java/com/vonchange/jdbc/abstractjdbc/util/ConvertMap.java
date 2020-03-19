package com.vonchange.jdbc.abstractjdbc.util;


import com.vonchange.jdbc.abstractjdbc.util.sql.OrmUtil;
import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.config.Constant;
import com.vonchange.mybatis.tpl.exception.MybatisMinRuntimeException;

import javax.persistence.Column;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConvertMap {
    private ConvertMap() {
        throw new IllegalStateException("Utility class");
    }

     public static   <T> Map<String,Object> toMap(Class<?> clazz,T entity) throws IntrospectionException {
         BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
         PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
         String propertyName;
         Object value;
         Map<String,Object> map = new HashMap<>();
         for (PropertyDescriptor property: propertyDescriptors) {
             propertyName = property.getName();
             value=property.getValue(propertyName);
             map.put(propertyName,value);
         }
         return map;
     }

    /**
     * 将一个 Map 对象转化为一个 JavaBean
     * @param type 要转化的类型
     * @param map 包含属性值的 map
     * @return 转化出来的 JavaBean 对象
     * @throws IntrospectionException 如果分析类属性失败
     * @throws IllegalAccessException 如果实例化 JavaBean 失败
     * @throws InstantiationException 如果实例化 JavaBean 失败
     */ 
    @SuppressWarnings("rawtypes") 
    public static Object convertMap(Class type, Map<String,Object> map) throws IntrospectionException, IllegalAccessException, InstantiationException {
        BeanInfo beanInfo = Introspector.getBeanInfo(type); // 获取类属性
        Object entity = null;
        try {
             entity = type.newInstance(); // 创建 JavaBean 对象
        }catch (InstantiationException e){
            throw new  MybatisMinRuntimeException("java.lang.InstantiationException "+type.getName()+" 实体类需要无参数构造函数");
        }
        if(null==map||map.isEmpty()){
            return  entity;
        }
        // 给 JavaBean 对象的属性赋值 
        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
        Class<?> propertyType;
        String propertyName;
        String fieldInEs;
        Object value;
        for (PropertyDescriptor property: propertyDescriptors) {
            propertyName = property.getName();
            propertyType=property.getPropertyType();
            Column column;
            try {
                column = type.getDeclaredField(propertyName).getAnnotation(Column.class);
            } catch (NoSuchFieldException e) {
                column=null;
            }
            fieldInEs=propertyName;
            if(null!=column&& StringUtils.isNotBlank(column.name())){
                fieldInEs=column.name();
            }
            fieldInEs=fieldInEs.toLowerCase();
            if (map.containsKey(fieldInEs)) {
                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
                value = map.get(fieldInEs);
                if(null==value){
                    continue;
                }
                //转换类型
                value= ConvertUtil.toObject(value,propertyType);
                Constant.BeanUtils.setProperty(entity, propertyName, value);
            }
        }
        return entity;
    }


    public static Map<String,Object> newMap(Map<String,Object> map){
        if(null==map||map.isEmpty()){
            return  new LinkedHashMap<>();
        }
        Map<String,Object> newMap=new LinkedHashMap<>();
        String key;
        for (Map.Entry<String,Object> entry: map.entrySet()) {
            key=entry.getKey();
            newMap.put(key.toLowerCase(),entry.getValue());
            //驼峰 转换
            key= OrmUtil.toFiled(entry.getKey());
            newMap.put(key.toLowerCase(),entry.getValue());
        }
        return newMap;
    }


}
