package com.vonchange.jdbc.abstractjdbc.util.mardown;


import com.vonchange.jdbc.abstractjdbc.config.ConstantJdbc;
import com.vonchange.jdbc.abstractjdbc.config.Constants;
import com.vonchange.jdbc.abstractjdbc.util.mardown.bean.MdWithInnerIdTemp;
import com.vonchange.jdbc.abstractjdbc.util.mardown.bean.SqlInfo;
import com.vonchange.mybatis.common.util.StringUtils;
import com.vonchange.mybatis.tpl.exception.MyRuntimeException;
import jodd.io.FileUtil;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * markdown 组件
 * von_change
 */
public class MarkdownUtil {
    private  MarkdownUtil(){
        throw new IllegalStateException("Utility class");
    }
    private   static Logger logger = LoggerFactory.getLogger(MarkdownUtil.class);
    private   static  Map<String,MarkdownDTO> idMardownMap=new ConcurrentHashMap<>();

    private   static  MarkdownDTO readMarkdownFile(String packname,String fileName){
        String url="file:/usr/etc/"+fileName;
        Resource resource= FileUtils.getResource(url);
        if(!resource.exists()){
             url= "classpath:"+FileUtils.getFileURLPath(packname,fileName);
             resource= FileUtils.getResource(url);
        }
       long lastModified;
        String id;
       try {
             lastModified=  resource.lastModified();
             if(lastModified==0){
                 return null;
             }
             id=resource.getURI().toString();
        } catch (IOException e) {
           logger.error("读取markdown文件出错{}:{}",url,e);
           return null;
        }
        boolean needLoad=true;
        if(null!=idMardownMap.get(id)){
            MarkdownDTO markdownDTO=idMardownMap.get(id);
            if((lastModified+"").equals(markdownDTO.getVersion())){
                needLoad=false;
            }
        }
        if(!needLoad){
            return  idMardownMap.get(id);
        }
        String content=null;
        try {
            content= FileUtil.readUTFString(resource.getInputStream());
        } catch (IOException e) {
            logger.error("读取markdown文件出错{}:{}",url,e);
        }
        MarkdownDTO markdownDTO= getMarkDownInfo(content,id,lastModified+"");
        idMardownMap.put(id,markdownDTO);
        return markdownDTO;
    }
    public  static  MarkdownDTO readMarkdown(String content,String id,String version){
        if(null==id||"".equals(id.trim())){
            id=getId(content);
        }
        if(null==version||"".equals(version.trim())){
            version=getVersion(content);
        }
        boolean needLoad=true;
        if(null!=idMardownMap.get(id)){
            MarkdownDTO markdownDTO=idMardownMap.get(id);
            if((version).equals(markdownDTO.getVersion())){
                needLoad=false;
            }
        }
        if(!needLoad){
            return  idMardownMap.get(id);
        }
        MarkdownDTO markdownDTO= getMarkDownInfo(content,id,version);
        idMardownMap.put(id,markdownDTO);
        return markdownDTO;
    }
    private   static  String getId(String result){
        String idSym= ConstantJdbc.MDID;
        int vdx = result.indexOf(idSym);
        if(vdx == -1) {
            throw new IllegalArgumentException("无"+ConstantJdbc.MDID);
        }
        StringBuilder idSB=new StringBuilder();
        for(int j=vdx+idSB.length();result.charAt(j)!='\n';j++){
            idSB.append(result.charAt(j));
        }
        return idSB.toString();
    }
    private   static  String getVersion(String result){
        String versionSym=ConstantJdbc.MDVERSION;
        int vdx = result.indexOf(versionSym);
        if(vdx == -1) {
            throw new IllegalArgumentException("无"+ConstantJdbc.MDVERSION);
        }
        StringBuilder versionSB=new StringBuilder();
        for(int j=vdx+versionSym.length();result.charAt(j)!='\n';j++){
            versionSB.append(result.charAt(j));
        }
        return versionSB.toString();
    }

    private static  MarkdownDTO getMarkDownInfo(String result,String id,String version){
        MarkdownDTO markdownDTO= new MarkdownDTO();
        String scriptSym="```";
        if(null==id||"".equals(id.trim())){
            markdownDTO.setId(getId(result));
        }else{
            markdownDTO.setId(id);
        }
        if(null==version||"".equals(version.trim())){
            markdownDTO.setVersion(getVersion(result));
        }else{
            markdownDTO.setVersion(version);
        }
        int i=0;
        int len = result.length();
        int startLen=scriptSym.length();
        int endLen=scriptSym.length();
        StringBuilder idSB;
        while (i < len) {
            int ndx = result.indexOf(scriptSym, i);
            if(ndx==-1){
                break;
            }
            ndx += startLen;
            idSB=new StringBuilder();
            for(int j=ndx;result.charAt(j)!='\n';j++){
                idSB.append(result.charAt(j));
            }
            int firstLineLength=idSB.length();
            idSB=new StringBuilder();
            for(int j=ndx+firstLineLength+1;result.charAt(j)!='\n';j++){
                idSB.append(result.charAt(j));
            }
            int zsIndex= idSB.indexOf(Constants.Markdown.IDPREF);
            if(zsIndex==-1){
                throw new IllegalArgumentException("无id注释"+idSB);
            }
            String key= idSB.substring(zsIndex+Constants.Markdown.IDPREF.length()).trim();
            if(key.length()==0){
                throw new IllegalArgumentException("请定义类型和ID at "+ndx);
            }
            ndx += idSB.length()+firstLineLength+1;
            int ndx2 = result.indexOf(scriptSym, ndx);
            if(ndx2 == -1) {
                throw new IllegalArgumentException("无结尾 ``` 符号 at: " + (ndx - startLen));
            }
            String content=result.substring(ndx,ndx2).trim();
            if(key.startsWith("json:")){
                // @TODO XXX
               /* markdownDTO.getJsonMap().put(key, JsonUtil.fromJson(content, new TypeReference<Map<String, Object>>() {
                }));*/
            }else{
                markdownDTO.getContentMap().put(key,content);
            }
            i=ndx2+endLen;
        }
        return markdownDTO;
    }
    public static   String getSqlSpinner(MarkdownDTO markdownDTO,String sql){
         if(StringUtils.isBlank(sql)){
             return sql;
         }
        if(!sql.contains("{@sql")){
            return  sql;
        }
        String startSym="{@sql";
        String endSym="}";
        int len = sql.length();
        int startLen=startSym.length();
        int endLen=endSym.length();
        int i=0;
        StringBuilder newSb=new StringBuilder();
        String model;
        while (i < len) {
            int ndx = sql.indexOf(startSym, i);
            if(ndx==-1){
                newSb.append(i == 0?sql:sql.substring(i));
                break;
            }
            newSb.append(sql.substring(i, ndx));
            ndx += startLen;
            int ndx2 = sql.indexOf(endSym, ndx);
            if(ndx2 == -1) {
                throw new IllegalArgumentException("无结尾 } 符号 at: " + (ndx - startLen));
            }
            model=sql.substring(ndx,ndx2).trim();
            newSb.append(MarkdownDataUtil.getSql(markdownDTO,model));
            i=ndx2+endLen;
        }
        return  newSb.toString();
    }

    public static String  getSql(String sqlId) {
        MdWithInnerIdTemp mdWithInnerIdTemp=loadConfigData(sqlId);
        String sql = MarkdownDataUtil.getSql(mdWithInnerIdTemp.getMarkdownDTO(),mdWithInnerIdTemp.getInnnerId());
        return sql;
    }
    public static SqlInfo getSqlInfo(String sqlId) {
        MdWithInnerIdTemp mdWithInnerIdTemp=loadConfigData(sqlId);
        String sql = MarkdownDataUtil.getSql(mdWithInnerIdTemp.getMarkdownDTO(),mdWithInnerIdTemp.getInnnerId());
        SqlInfo sqlInfo=new SqlInfo();
        sqlInfo.setInnnerId(mdWithInnerIdTemp.getInnnerId());
        sqlInfo.setMarkdownDTO(mdWithInnerIdTemp.getMarkdownDTO());
        sqlInfo.setSql(sql);
        return sqlInfo;
    }
    public   static MdWithInnerIdTemp loadConfigData(String sqlId) {
        MdWithInnerIdTemp mdWithInnerIdTemp=new MdWithInnerIdTemp();
        if(sqlId.startsWith(ConstantJdbc.ISSQLFLAG)){
            MarkdownDTO markdownDTO= new MarkdownDTO();
            markdownDTO.setId(ConstantJdbc.MAINFLAG+"_"+ StringUtils.uuid());
            Map<String,String> map=new HashMap<>();
            String sql=sqlId.substring(ConstantJdbc.ISSQLFLAG.length());
            if(sqlId.endsWith(ConstantJdbc.COUNTFLAG)){
                sql=null;
            }
            map.put(ConstantJdbc.MAINFLAG,sql);
            markdownDTO.setContentMap(map);
            String innerId=ConstantJdbc.MAINFLAG;
            if(sqlId.endsWith(ConstantJdbc.COUNTFLAG)){
                innerId="main"+ConstantJdbc.COUNTFLAG;
            }
            mdWithInnerIdTemp.setInnnerId(innerId);
            mdWithInnerIdTemp.setMarkdownDTO(markdownDTO);
            return mdWithInnerIdTemp;
        }
        if(sqlId.startsWith(ConstantJdbc.ISMDFLAG)){
            MarkdownDTO markdownDTO= MarkdownUtil.readMarkdown(sqlId.substring(ConstantJdbc.ISMDFLAG.length()),null,null);
            mdWithInnerIdTemp.setInnnerId(ConstantJdbc.MAINFLAG);
            mdWithInnerIdTemp.setMarkdownDTO(markdownDTO);
            return mdWithInnerIdTemp;
        }

        String[] sqlIds = StringUtil.split(sqlId, ".");
        if (sqlIds.length < 2) {
            throw  new MyRuntimeException("获取配置文件id有误:"+sqlId);
        }
        StringBuilder packageName = new StringBuilder();
        for (int i = 0; i < sqlIds.length - 2; i++) {
            packageName = packageName.append(sqlIds[i]);
        }
        String fileName=sqlIds[sqlIds.length - 2] + ".md";
        String needFindId=sqlIds[sqlIds.length - 1];
        MarkdownDTO markdownDTO= MarkdownUtil.readMarkdownFile(packageName.toString(),fileName);
        mdWithInnerIdTemp.setInnnerId(needFindId);
        mdWithInnerIdTemp.setMarkdownDTO(markdownDTO);
        return mdWithInnerIdTemp;
    }
    public static  String initStringMd(String md){
        return  initStringMd(md,StringUtils.uuid(),ConstantJdbc.MDINITVERSION);
    }
    public static  String initStringMd(String md,String id,String version){
        if(!md.contains(ConstantJdbc.MDVERSION)){
            md=ConstantJdbc.MDVERSION+" "+version+" \n"+md;
        }
        if(!md.contains(ConstantJdbc.MDID)){
            md=ConstantJdbc.MDID+" "+id + "\n"+md;
        }
        if(!md.startsWith(ConstantJdbc.ISMDFLAG)){
            md=ConstantJdbc.ISMDFLAG+md;
        }
        return  md;
    }
    public static  String initSqlInSqlId(String sqlId){
        if(!sqlId.startsWith(ConstantJdbc.ISSQLFLAG)){
            return  ConstantJdbc.ISSQLFLAG+sqlId;
        }
        return  sqlId;
    }

}
