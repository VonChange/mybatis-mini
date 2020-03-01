package com.vonchange.jdbc.abstractjdbc.util.markdown;


import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.jdbc.abstractjdbc.config.Constants;
import com.vonchange.jdbc.abstractjdbc.util.markdown.bean.CacheInfo;
import com.vonchange.jdbc.abstractjdbc.util.markdown.bean.JoinTable;
import com.vonchange.jdbc.abstractjdbc.util.markdown.bean.MemTable;
import com.vonchange.mybatis.common.util.ConvertUtil;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.tpl.exception.MybatisMinRuntimeException;
import jodd.util.StringUtil;

import java.util.Map;


public class MarkdownDataUtil{
    private MarkdownDataUtil() {
        throw new IllegalStateException("Utility class");
    }
    private  static String notFindIdMsg="查找不到该ID或ID内内容为空";
    public static JoinTable getJoinTable(MarkdownDTO markdownDTO, String id) {
        JoinTable joinTable= new JoinTable();
        String sql=getSql(markdownDTO,id);
        if(StringUtils.isBlank(sql)){
            throw  new MybatisMinRuntimeException(markdownDTO.getId()+notFindIdMsg+ Constants.Markdown.TABLE+id);
        }
        Map<String,Object> data= markdownDTO.getJsonMap().get(Constants.Markdown.JSON+id);
        if(null==data){
            throw  new MybatisMinRuntimeException(markdownDTO.getId()+notFindIdMsg+Constants.Markdown.JSON+id);
        }
        String js=markdownDTO.getContentMap().get(Constants.Markdown.JS+id);
        if(!StringUtil.isBlank(js)){
            joinTable.setJs(js);
        }
        joinTable.setSql(sql);
        joinTable.setTables(ConvertUtil.toString(data.get(Constants.Markdown.TABLES)));
        joinTable.setViews(ConvertUtil.toString(data.get(Constants.Markdown.VIEWS)));
        return  joinTable;
    }

    public static MemTable getMemTable(MarkdownDTO markdownDTO, String id) {
        MemTable memTable = new MemTable();
        String table=markdownDTO.getContentMap().get(Constants.Markdown.TABLE+id);
        if(StringUtils.isBlank(table)){
            throw  new MybatisMinRuntimeException(markdownDTO.getId()+notFindIdMsg+Constants.Markdown.TABLE+id);
        }
        Map<String,Object> data= markdownDTO.getJsonMap().get(Constants.Markdown.JSON+id);
        memTable.setSql(table);
        memTable.setInfo(data);
        return  memTable;
    }
    public static MemTable getViewTable(MarkdownDTO markdownDTO, String id) {
        MemTable memTable = new MemTable();
        String view=markdownDTO.getContentMap().get(Constants.Markdown.VIEW+id);
        if(StringUtils.isBlank(view)){
            throw  new MybatisMinRuntimeException(markdownDTO.getId()+notFindIdMsg+Constants.Markdown.VIEW+id);
        }
        Map<String,Object> data= markdownDTO.getJsonMap().get(Constants.Markdown.JSON+id);
        memTable.setSql(view);
        memTable.setInfo(data);
        return  memTable;
    }
    public static CacheInfo getCacheInfo(MarkdownDTO markdownDTO, String id) {
        CacheInfo cacheInfo= new CacheInfo();
        Map<String,Object> data= markdownDTO.getJsonMap().get(Constants.Markdown.JSON+id);
        if(null==data){
            cacheInfo.setCache(false);
            return  cacheInfo;
        }
        Boolean cache=ConvertUtil.toBoolean(data.get(Constants.Markdown.Cache.CACHE));
        if(null==cache||!cache){
            cacheInfo.setCache(false);
            return  cacheInfo;
        }
        String timeStr=ConvertUtil.toString(data.get(Constants.Markdown.Cache.TIME));
        if(null==timeStr){
            timeStr="10";
        }
       String[] times= timeStr.split("\\*");
        Long time=1L;
        for (String timeSub: times) {
            time=ConvertUtil.toLong(timeSub)*time;
        }
        String sizeStr=ConvertUtil.toString(data.get(Constants.Markdown.Cache.SIZE));
        if(null==sizeStr){
            sizeStr="1";
        }
        String[] sizes= sizeStr.split("\\*");
        Long size=1L;
        for (String sizeSub: sizes) {
            size=ConvertUtil.toLong(sizeSub)*size;
        }
        cacheInfo.setCache(cache);
        cacheInfo.setTime(1L==time?0L:time);
        cacheInfo.setSize(1L==size?0L:size);
        return cacheInfo;
    }
    public static String getSql(MarkdownDTO markdownDTO,String id) {
        String sql =markdownDTO.getContentMap().get(Constants.Markdown.SQL+id);
        if(StringUtils.isBlank(sql)&&!StringUtils.endsWith(id, ConstantJdbc.COUNTFLAG)){
            throw  new MybatisMinRuntimeException(markdownDTO.getId()+"查找不到该ID或ID内内容为空"+Constants.Markdown.SQL+id);
        }
        /* 支持{@sql */
        return MarkdownUtil.getSqlSpinner(markdownDTO,sql);
    }
}
