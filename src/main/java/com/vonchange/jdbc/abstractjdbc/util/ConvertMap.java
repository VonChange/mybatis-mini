package com.vonchange.jdbc.abstractjdbc.util;


import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.tpl.OrmUtil;
import com.vonchange.mybatis.tpl.exception.MybatisMinRuntimeException;

import javax.persistence.Column;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConvertMap {
    private ConvertMap() {
        throw new IllegalStateException("Utility class");
    }

     public static  Map<String,Object> toMap(Class<?> clazz) throws IntrospectionException {
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
     *  Map to JavaBean
     */ 
    @SuppressWarnings("rawtypes") 
    public static <T> T convertMap(T entity,Class type, Map<String,Object> map) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        if(null!=entity){
            type=entity.getClass();
        }
        if(null==entity){
            try {
                entity = (T) type.newInstance();
            }catch (InstantiationException e){
                throw new  MybatisMinRuntimeException("java.lang.InstantiationException "+type.getName()+" need no-arguments constructor");
            }
        }
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        if(null==map||map.isEmpty()){
            return  entity;
        }
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
                value= ConvertUtil.toObject(value,propertyType);
                Method writeMethod = property.getWriteMethod();
                writeMethod.invoke(entity,value);
            }
        }
        return entity;
    }
    public static <T> T convertMap(Class type, Map<String,Object> map) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
         return  convertMap(null,type,map);
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
            key= OrmUtil.toFiled(entry.getKey());
            newMap.put(key.toLowerCase(),entry.getValue());
        }
        return newMap;
    }


}
