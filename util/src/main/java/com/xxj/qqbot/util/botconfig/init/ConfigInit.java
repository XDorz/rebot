package com.xxj.qqbot.util.botconfig.init;

import cn.hutool.core.img.ImgUtil;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.*;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.Image;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.image.ClassTypeConstant;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.image.ImageCacheConf;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.*;
import com.xxj.qqbot.util.common.ConfigLoaderUtil;
import com.xxj.qqbot.util.common.MoriBotException;
import com.xxj.qqbot.util.common.MoriException;
import com.xxj.qqbot.util.constant.BotConfigTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 为所有标有 BotConfig 的类的属性赋值
 * 框架中最优先初始化
 */
@Slf4j
@Component
public class ConfigInit {

    /**
     * 初始化推迟至spring环境变量注入之后
     */
    @Autowired
    EnvironmentInit environmentInit;

    @Autowired
    Environment environment;

    /**
     * 初始化配置文件
     */
    @PostConstruct
    private void init(){
        //初始化图片缓存默认配置信息
        //当读取到图片config时需要确认默认配置
        //所以置于此处
        try {
            initDefaultImageConfig();
        } catch (Exception e) {
            log.error("图片设置出现未知错误，请检查自定义图片缓存是否设置正确",e);
        }
        log.info("开始读取配置文件");
        List<Class<?>> clazzs = ConfigLoaderUtil.fetchAnnoClass(BotConfig.class);
        for (Class<?> clazz : clazzs) {
            boolean mayImage=false;
            BotConfig clazzAnno = clazz.getAnnotation(BotConfig.class);
            Image clazzImage = clazz.getAnnotation(Image.class);
            PathUrl clazzPathUrl = clazz.getAnnotation(PathUrl.class);
            boolean classSavePath= clazzPathUrl != null && clazzPathUrl.saveFilePath();
            Map<String, String> reflexMap = getReflexMap(clazzAnno);
            String path = analysisPath(clazzAnno, reflexMap, clazz.getName() + ":");
            if(clazzImage !=null||clazzAnno.type().equals(BotConfigTypeEnum.IMAGE_ID_TYPE)
                    ||clazzAnno.type().equals(BotConfigTypeEnum.IMAGE_PATH_TYPE)){
                mayImage=true;
            }
            Map<String,List<Object>>[] cache=new HashMap[3];
            for (Field field : clazz.getFields()) {
                BotConfig botConfig = field.getAnnotation(BotConfig.class);
                ConfigExclude configExclude = field.getAnnotation(ConfigExclude.class);
                ConfigKey configKey = field.getAnnotation(ConfigKey.class);
                MultiLineConfig multiLineConfig = field.getAnnotation(MultiLineConfig.class);
                Image imageAnno = field.getAnnotation(Image.class);
                PathUrl pathUrl = field.getAnnotation(PathUrl.class);
                ConfigAlias alias = field.getAnnotation(ConfigAlias.class);
                //置入容器
                if(alias!=null){
                    if(BotFrameworkConfig.injectedFieldHolder.containsKey(alias.name())){
                        log.error(clazz.getName()+"---"+field.getName()+"---已有别名为"+alias.name()+"的属性，前值将被后值覆盖！");
                    }
                    BotFrameworkConfig.injectedFieldHolder.put(alias.name(),field);
                }else {
                    if(BotFrameworkConfig.injectedFieldHolder.containsKey(field.getName())){
                        log.error(clazz.getName()+"---"+field.getName()+"---已有名为"+field.getName()+"的属性，前值将被后值覆盖！");
                    }
                    BotFrameworkConfig.injectedFieldHolder.put(field.getName(),field);
                }

                if(configExclude!=null) continue ;
                String name=field.getName();
                if(configKey!=null){
                    name=configKey.name();
                }
                if (botConfig!=null){
                    String fieldPath = analysisPath(botConfig, reflexMap, clazz.getName() + "---" + field.getName() + ":");
                    if(!StringUtils.hasText(fieldPath)){
                        log.error(clazz.getName()+"---"+field.getName()+ ":指定的路径解析为空，请检查！");
                    }
                    //如果有标注为图片则为其赋值
                    if(imageAnno !=null||botConfig.type().equals(BotConfigTypeEnum.IMAGE_ID_TYPE)
                            ||botConfig.type().equals(BotConfigTypeEnum.IMAGE_PATH_TYPE)||judgeNeedInjectImage(field)){
                        ImageCacheConf config=null;
                        //优先级 属性上的ImageConfig>>属性上的botImageConfig的type>>类上的ImageConfig>>类上的botImageConfig的type>>默认
                        //由于标注在属性上的BotConfig只针对这个属性
                        //故不再针对找不到配置的属性尝试注入Image
                        if(imageAnno !=null){
                            config=new ImageCacheConf(clazz,field,fieldPath, imageAnno);
                        }else if(botConfig.type().equals(BotConfigTypeEnum.IMAGE_ID_TYPE)
                                ||botConfig.type().equals(BotConfigTypeEnum.IMAGE_PATH_TYPE)){
                            if(clazzImage!=null){
                                config=new ImageCacheConf(clazz,field,fieldPath, clazzImage);
                                config.setSaveId(botConfig.type());
                            }else {
                                config=new ImageCacheConf(clazz,field,fieldPath, botConfig.type());
                            }
                        }else if(clazzImage !=null){
                            config=new ImageCacheConf(clazz,field,fieldPath, clazzImage);
                        }else if(clazzAnno.type().equals(BotConfigTypeEnum.IMAGE_ID_TYPE)
                                ||clazzAnno.type().equals(BotConfigTypeEnum.IMAGE_PATH_TYPE)){
                            config=new ImageCacheConf(clazz,field,fieldPath,clazzAnno.type());
                        }else {
                            config=new ImageCacheConf(clazz,field,fieldPath,!classSavePath);
                        }
                        if(pathUrl!=null){
                            config.setSaveId(!pathUrl.saveFilePath());
                        }
                        putImageMap(fieldPath,config);
                        continue ;
                    }
                    if(botConfig.variablePrefix().length!=0){
                        log.warn(clazz.getName()+"---"+field.getName()+"注解在属性上的variablePrefix将被忽略！");
                    }
                    Map<String, List<Object>> fieldMap=null;

                    //属性的类型判断最优先，其次是看@MultiLineConfig注解，再次是看@PathUrl注解
                    //再看属性上的botConfig的multiLine属性
                    //最后是默认的配置项值注入
                    if(field.getGenericType().getTypeName().contains(ClassTypeConstant.FILE)||
                            field.getGenericType().getTypeName().contains(ClassTypeConstant.FONT)){
                        fieldMap=ConfigLoaderUtil.getFileMap(fieldPath);
                    }else if(multiLineConfig!=null&&multiLineConfig.multiLine()){
                        fieldMap=ConfigLoaderUtil.getFileVal(fieldPath,true);
                    }else if(pathUrl!=null&&pathUrl.saveFilePath()){
                        fieldMap=ConfigLoaderUtil.getFileMap(fieldPath);
                    }else if(botConfig.multiLine().length!=0&&botConfig.multiLine()[0]){
                        fieldMap=ConfigLoaderUtil.getFileVal(fieldPath,botConfig.multiLine()[0]);
                    }else if(classSavePath){
                        fieldMap=ConfigLoaderUtil.getFileMap(fieldPath);
                    }else if(clazzAnno.multiLine().length!=0&&clazzAnno.multiLine()[0]){
                        fieldMap=ConfigLoaderUtil.getFileVal(fieldPath,clazzAnno.multiLine()[0]);
                    }else {
                        fieldMap=ConfigLoaderUtil.getFileVal(fieldPath,false);
                    }
                    try {
                        setValue(field,name,fieldMap);
                    } catch (IllegalAccessException|MoriException|MoriBotException e){
                        log.error(clazz.getName()+"---"+field.getName()+"---"+field.getGenericType()+"---"+e.getMessage());
                    }
                }else {
                    if(!StringUtils.hasText(path)){
                        log.error(clazz.getName() + ":指定的路径解析为空，请检查！");
                    }
                    //如果属性上标有ImageConfig注解或属性本身是个Image类的话
                    if(imageAnno !=null||judgeNeedInjectImage(field)){
                        ImageCacheConf config=null;
                        //优先级 属性上的ImageConfig>>类上的ImageConfig>>类上的botImageConfig的type>>默认
                        if(imageAnno !=null){
                            config=new ImageCacheConf(clazz,field,path, imageAnno);
                        }else if(clazzImage !=null){
                            config=new ImageCacheConf(clazz,field,path, clazzImage);
                        }else if(clazzAnno.type().equals(BotConfigTypeEnum.IMAGE_ID_TYPE)
                                ||clazzAnno.type().equals(BotConfigTypeEnum.IMAGE_PATH_TYPE)){
                            config=new ImageCacheConf(clazz,field,path,clazzAnno.type());
                        }else {
                            config=new ImageCacheConf(clazz,field,path,!classSavePath);
                        }
                        if(pathUrl!=null){
                            config.setSaveId(!pathUrl.saveFilePath());
                        }
                        putImageMap(path,config);
                        continue ;
                    }

                    //0为 储存文件的map
                    //1为 储存配置项的map
                    //2位 储存多行值合并的map
                    int type=-1;
                    if(field.getGenericType().getTypeName().contains(ClassTypeConstant.FILE)||
                            field.getGenericType().getTypeName().contains(ClassTypeConstant.FONT)){
                        type=0;
                    }else if(multiLineConfig!=null&&multiLineConfig.multiLine()){
                        type=2;
                    }else if(pathUrl!=null&&pathUrl.saveFilePath()){
                        type=0;
                    }else if(classSavePath){
                        type=0;
                    }else if(clazzAnno.multiLine().length!=0&&clazzAnno.multiLine()[0]){
                        type=2;
                    }else {
                        type=1;
                    }
                    if(cache[type]==null){
                        switch (type){
                            case 0:
                                cache[0]=ConfigLoaderUtil.getFileMap(path);
                                break;
                            case 1:
                                cache[1]=ConfigLoaderUtil.getFileVal(path,false);
                                break;
                            case 2:
                                cache[2]=ConfigLoaderUtil.getFileVal(path,true);
                                break;
                        }
                    }
                    Map<String,List<Object>> valMap=cache[type];
                    try {
                        setValue(field,name,valMap);
                    } catch (IllegalAccessException|MoriException e) {
                        log.error(clazz.getName()+"---"+field.getName()+"---"+field.getGenericType()+"---"+e.getMessage());
                    }catch (MoriBotException e){
                        //如果类中标有ImageConfig注解或BotConfig注解的type有表明这是个需要缓存Image的类的话
                        //将在找不到可注入的配置文件时改为注入image缓存
                        if(mayImage){
                            ImageCacheConf config=null;
                            //优先级 类上的ImageConfig>>类上的botImageConfig的type
                            if(clazzImage !=null){
                                config=new ImageCacheConf(clazz,field,path, clazzImage);
                            }else if(clazzAnno.type().equals(BotConfigTypeEnum.IMAGE_ID_TYPE)
                                    ||clazzAnno.type().equals(BotConfigTypeEnum.IMAGE_PATH_TYPE)){
                                config=new ImageCacheConf(clazz,field,path,clazzAnno.type());
                            }
                            putImageMap(path,config);
                        }else {
                            log.error(clazz.getName()+"---"+field.getName()+"---"+field.getGenericType()+"---"+e.getMessage());
                        }
                    }
                }
            }
        }
        initExtract();
    }

    /**
     * 初始化图像缓存默认设置
     */
    private static void initDefaultImageConfig() throws
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if(BotFrameworkConfig.imageMap==null){
            BotFrameworkConfig.imageMap=new HashMap<>();
        }
        List<Class<? extends ImageCacheConfig>> configClasses = ConfigLoaderUtil.fetchConcreteClass(ImageCacheConfig.class);
        configClasses.remove(DefaultEventConfig.class);
        ImageCacheConfig cacheConfig=null;
        if(configClasses.size()!=0){
            cacheConfig=configClasses.get(0).getConstructor().newInstance();
        }else {
            cacheConfig = new DefaultImageCacheConfig();
        }
        BotFrameworkConfig.cacheDay=cacheConfig.generateCacheDay();
        BotFrameworkConfig.autoImageUpdate=cacheConfig.generateUpdate();
        BotFrameworkConfig.useRandomNum=cacheConfig.generateRandomNum();
        BotFrameworkConfig.stringSaveImageId=!cacheConfig.stringSaveImagePath();
    }

    /**
     * 从注解中解析出文件路
     *
     * @param botConfig             注解
     * @param reflex                前缀映射
     * @param exceptionDesc         出现异常时的描述信息
     * @return
     */
    private String analysisPath(BotConfig botConfig,Map<String,String> reflex,String exceptionDesc){
        //匹配并替换${}的内容
        Pattern pattern = Pattern.compile("\\$\\{([^\\$\\{\\}]*?)\\}");
        String source=botConfig.source();
        String path=source;
        Matcher matcher = pattern.matcher(path);
        while (matcher.find()){
            String val=matcher.group(1);
            if(reflex.containsKey(val)) val=reflex.get(val);
            //由于"\"符号会被转义，故需要替换为"\\"
            String enval="";
            if((enval=environment.getProperty(val))==null){
                enval=val;
                log.warn(exceptionDesc+"未在spring环境变量中找到["+val+"]有关值，将按原样赋值");
            }
            path=matcher.replaceFirst(enval.replaceAll("\\\\", "\\\\\\\\"));
            matcher=pattern.matcher(path);
        }
        return path;

    }

    /**
     * 将map的值set进属性中
     *
     * @param field                             将被注入的属性
     * @param name                              将从配置中读取的key
     * @param map                               配置存放容器
     * @throws IllegalAccessException           反射操作问题
     * @throws MoriException                    配置文件问题
     * @throws MoriBotException                 可能要当成image处理的Exception
     */
    private void setValue(Field field,String name,Map<String,List<Object>> map)
            throws IllegalAccessException,MoriException,MoriBotException{
        if(name==null||name.equals("")){
            name=field.getName();
        }
        List<Object> value = null;
        if(Map.class.isAssignableFrom(field.getType())){
            if(field.getGenericType().getTypeName().contains(ClassTypeConstant.LIST)){
                field.set(null,convertMapVal(map,field,true));
            }else {
                field.set(null,convertMapVal(map,field,false));
            }
        }else {
            if(map.size()==1){
                Object key=map.keySet().toArray()[0];
                value = map.get(key);
            }else {
                if(!map.containsKey(name)){
                    if(List.class.isAssignableFrom(field.getType())){
                        value=new ArrayList();
                        for (String key : map.keySet()) {
                            value.addAll(map.get(key));
                        }
                    }else {
                        throw new MoriBotException("读取文件值中没有"+name+"字段相关内容，该字段未赋值！");
                    }
                }else {
                    value=map.get(name);
                }
            }
            setValue(field,value);
        }
    }


    /**
     * 将map内的值转换为其他类型
     *
     * @param original                  读取的配置文件数据
     * @param target                    要转换的目标属性
     * @param isMapList                 给的是否是fileMap
     * @return
     * @throws MoriException
     */
    private Map convertMapVal(Map<String,List<Object>> original,Field target,boolean isMapList) throws MoriException{
        //验证传入map是否是存放file的map
        boolean isFileMap=false;
        for (String key : original.keySet()) {
            if(original.get(key).size()==0) continue;
            if(File.class.isAssignableFrom(original.get(key).get(0).getClass())){
                isFileMap=true;
            }
            break;
        }
        //解析出map中value的泛型
        String name = target.getGenericType().getTypeName();
        name=name.substring(name.lastIndexOf(",")+1);
        name=name.replace(ClassTypeConstant.LIST,"");
        name=name.replace(">","");
        name=name.replace(">","");
        name=name.trim();
        try {
            if(isMapList){
                return convertMapList(original,Class.forName(name),isFileMap);
            }else {
                return convertMap(original,Class.forName(name),isFileMap);
            }
        }catch (ClassNotFoundException e){
            throw new MoriException("无法解析map的泛型类型，请确保map有明确的泛型类型");
        }
    }

    /**
     * 将读取的配置文件转换为对应的map类型
     *
     * @param original                  读取的配置文件
     * @param target                    转换的类型
     * @param <T>                       转换的类型
     * @return
     * @throws MoriException
     */
    @SuppressWarnings("unchecked")
    private <T> Map<String,List<T>>  convertMapList(Map<String,List<Object>> original,Class<T> target,boolean isFileMap) throws MoriException{
        Map<String,List<T>> map=new HashMap<>();
        if(Integer.class.isAssignableFrom(target)){
            if(isFileMap){
                throw new MoriException("无法将读取到的File转换为Integer类型，请在该属性上加入@PathUrl并配置对应属性以读取文件内配置项！");
            }
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                List<T> list=new ArrayList<>();
                for (Object o : origList) {
                    try {
                        list.add(((T)(Integer.valueOf(o.toString()))));
                    }catch (Exception e){
                        throw new MoriException("无法将读取到的配置项转为Integer");
                    }
                }
                map.put(key,list);
            }
        }else if(Long.class.isAssignableFrom(target)){
            if(isFileMap){
                throw new MoriException("无法将读取到的File转换为Long类型，请在该属性上加入@PathUrl并配置对应属性以读取文件内配置项！");
            }
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                List<T> list=new ArrayList<>();
                for (Object o : origList) {
                    try {
                        list.add(((T)(Long.valueOf(o.toString()))));
                    }catch (Exception e){
                        throw new MoriException("无法将读取到的配置项转为Long");
                    }
                }
                map.put(key,list);
            }
        }else if(String.class.isAssignableFrom(target)){
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                List<T> list=new ArrayList<>();
                for (Object o : origList) {
                    if(isFileMap){
                        list.add(((T)((File)o).getAbsolutePath()));
                    }else {
                        list.add(((T)(o.toString())));
                    }
                }
                map.put(key,list);
            }
        }else if(File.class.isAssignableFrom(target)){
            if(!isFileMap){
                throw new MoriException("无法将读取到的配置文件项转换为File类型，请在该属性上加入@PathUrl并配置对应属性以记录该file！");
            }
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                List<T> list=new ArrayList<>();
                for (Object o : origList) {
                    list.add(((T)o));
                }
                map.put(key,list);
            }
        }else if(Font.class.isAssignableFrom(target)){
            if(!isFileMap){
                throw new MoriException("无法将读取到的配置文件项转换为Font类型，请在该属性上加入@PathUrl并配置对应属性以记录该file！");
            }
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                List<T> list=new ArrayList<>();
                for (Object o : origList) {
                    list.add((T)(ImgUtil.createFont((File)o)));
                }
                map.put(key,list);
            }
        }else {
            throw new MoriException("Map配置文件保存的Value类型仅支持String，File，Long，Integer类型");
        }
        return map;
    }

    /**
     * 将读取的配置文件转换为对应的map类型
     *
     * @param original                  读取的配置文件
     * @param target                    转换的类型
     * @param <T>                       转换的类型
     * @return
     * @throws MoriException
     */
    @SuppressWarnings("unchecked")
    private <T> Map<String,T>  convertMap(Map<String,List<Object>> original,Class<T> target,boolean isFileMap) throws MoriException{
        Map<String,T> map=new HashMap<>();
        if(Integer.class.isAssignableFrom(target)){
            if(isFileMap){
                throw new MoriException("无法将读取到的File转换为Integer类型，请在该属性上加入@PathUrl并配置对应属性以读取文件内配置项！");
            }
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                int i=0;
                for (Object o : origList) {
                    try {
                        T t = (T) (Integer.valueOf(o.toString()));
                        if(i==0){
                            map.put(key,t);
                        }else {
                            map.put(key+i,t);
                        }
                        i++;
                    }catch (Exception e){
                        throw new MoriException("无法将读取到的配置项转为Integer");
                    }
                }
            }
        }else if(Long.class.isAssignableFrom(target)){
            if(isFileMap){
                throw new MoriException("无法将读取到的File转换为Long类型，请在该属性上加入@PathUrl并配置对应属性以读取文件内配置项！");
            }
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                int i=0;
                for (Object o : origList) {
                    try {
                        T t = (T) (Long.valueOf(o.toString()));
                        if(i==0){
                            map.put(key,t);
                        }else {
                            map.put(key+i,t);
                        }
                        i++;
                    }catch (Exception e){
                        throw new MoriException("无法将读取到的配置项转为Long");
                    }
                }
            }
        }else if(String.class.isAssignableFrom(target)){
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                int i=0;
                for (Object o : origList) {
                    T t=null;
                    if(isFileMap){
                        t = (T)(((File)o).getAbsolutePath());
                    }else {
                        t = (T) (o.toString());
                    }
                    if(i==0){
                        map.put(key,t);
                    }else {
                        map.put(key+i,t);
                    }
                    i++;
                }
            }
        }else if(File.class.isAssignableFrom(target)){
            if(!isFileMap){
                throw new MoriException("无法将读取到的配置文件项转换为File类型，请在该属性上加入@PathUrl并配置对应属性以记录该file！");
            }
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                int i=0;
                for (Object o : origList) {
                    T t=((T)o);
                    if(i==0){
                        map.put(key,t);
                    }else {
                        map.put(key+i,t);
                    }
                    i++;
                }
            }
        }else if(Font.class.isAssignableFrom(target)){
            if(!isFileMap){
                throw new MoriException("无法将读取到的配置文件项转换为Font类型，请在该属性上加入@PathUrl并配置对应属性以记录该file！");
            }
            for (String key : original.keySet()) {
                List<?> origList = original.get(key);
                int i=0;
                for (Object o : origList) {
                    T t = (T) (ImgUtil.createFont((File) o));
                    if(i==0){
                        map.put(key,t);
                    }else {
                        map.put(key+i,t);
                    }
                    i++;
                }
            }
        }else {
            throw new MoriException("Map配置文件保存的Value类型仅支持String，File，Long，Integer类型");
        }
        return map;
    }

    /**
     * 判断list内元素是否只有一个
     *
     * @param list                  被判断的链表
     * @throws MoriException        配置文件加载Exception
     */
    private static void assertOne(List<?> list)throws MoriException{
        if(list.size()>1){
            throw new MoriException("该属性对应配置文件中的参数有"+list.size()+"个，类中属性仅要求一个！");
        }
        if(list.size()==0){
            throw new MoriException("该属性要求的配置文件中没有对应参数！");
        }
    }

    /**
     * 为属性赋值,
     *
     * @param field                         要赋值的属性
     * @param value                         装载属性值的的链表
     * @throws MoriException                配置文件问题
     * @throws MoriBotException             可能要当成image处理的Exception
     * @throws IllegalAccessException       反射操作问题
     */
    @SuppressWarnings("unchecked")
    private static void setValue(Field field,List<Object> value) throws MoriException,MoriBotException,IllegalAccessException{
        if(value==null){
            throw new MoriBotException("未从对应配置文件中读取到与属性对应的配置信息！");
        }
        if(List.class.isAssignableFrom(field.getType())){
            Class<?> clz=String.class;
            List list=new ArrayList<String>();
            if(field.getGenericType().getTypeName().contains(ClassTypeConstant.INTEGER)){
                clz=Integer.class;
                list=new ArrayList<Integer>();
            }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.LONG)){
                clz=Long.class;
                list=new ArrayList<Long>();
            }else if(field.getGenericType().getTypeName().contains("<"+ClassTypeConstant.STRING+">")){
                clz=String.class;
                list=new ArrayList<String>();
            }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.FILE)){
                clz=File.class;
                list=new ArrayList<File>();
            }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.FONT)){
                clz=Font.class;
                list=new ArrayList<Font>();
            }
            for (Object s : value) {
                list.add(parseValue(s,clz));
            }
            field.set(null,list);
        }else{
            assertOne(value);
            field.set(null,parseValue(value.get(0),field.getType()));
        }
    }

    /**
     * 将对象转换为对应类型
     *
     * @param obj               原始对象
     * @param clazz             目标类型
     * @return
     * @throws MoriException    配置文件问题
     */
    private static Object parseValue(Object obj,Class<?> clazz) throws MoriException{
        if(Integer.class.isAssignableFrom(clazz)){
            if(!(obj instanceof String)){
                throw new MoriBotException("若要注入Integer类型，请保证该属性不受@PathUrl注解影响！");
            }
            return Integer.parseInt(obj.toString());
        }else if(Long.class.isAssignableFrom(clazz)){
            if(!(obj instanceof String)){
                throw new MoriBotException("若要注入Long类型，请保证该属性不受@PathUrl注解影响！");
            }
            return Long.parseLong(obj.toString());
        }else if(String.class.isAssignableFrom(clazz)){
            if(obj instanceof File){
                return ((File) obj).getAbsoluteFile();
            }
            return obj.toString();
        }else if(File.class.isAssignableFrom(clazz)){
            if(!(obj instanceof File)){
                throw new MoriBotException("若要注入Font类型，请保证该属性的备选值为文件类型详见@PathUrl注解！");
            }
            return obj;
        }else if(Font.class.isAssignableFrom(clazz)){
            if(!(obj instanceof File)){
                throw new MoriBotException("若要注入Font类型，请保证该属性的备选值为文件类型详见@PathUrl注解！");
            }
            return ImgUtil.createFont((File)obj);
        }
        throw new MoriException("不被允许的属性类型");
    }

    /**
     * 获取BotConfig中的variablePrefix属性并将其映射入map
     */
    private static Map<String,String> getReflexMap(BotConfig botConfig){
        Map<String,String> reflex=new HashMap<>();
        String[] prefixes = botConfig.variablePrefix();
        for (String prefix : prefixes) {
            if(prefix.contains("=")){
                String[] split = prefix.split("=");
                reflex.put(split[0],split[1]);
            }
        }
        return reflex;
    }

    /**
     * 应用用户自定义初始化方法
     */
    private static void initExtract(){
        List<Class<? extends ExtractBotConfig>> extractClazz = ConfigLoaderUtil.fetchConcreteClass(ExtractBotConfig.class);
        for (Class<? extends ExtractBotConfig> clazz : extractClazz) {
            Object obj = null;
            try {
                obj = clazz.getConstructor().newInstance();
            } catch (InstantiationException e) {
                log.error(clazz.getName()+"---该类无法创建实例",e);
            } catch (IllegalAccessException e) {
                log.error(clazz.getName()+"---该类无法操作",e);
            } catch (InvocationTargetException e) {
                log.error(clazz.getName()+"---该类返回对象类型不符",e);
            } catch (NoSuchMethodException e) {
                log.error(clazz.getName()+"---该类没有无参构造器",e);
            }
            try {
                ((ExtractBotConfig)obj).initex();
            }catch (Exception e){
                log.error(clazz.getName()+"----初始化额外配置失败,配置部分加载",e);
            }
        }
    }

    private boolean judgeNeedInjectImage(Field field){
        if(net.mamoe.mirai.message.data.Image.class.isAssignableFrom(field.getType())) return true;
        if(BufferedImage.class.isAssignableFrom(field.getType())) return true;
        if(field.getGenericType().getTypeName().contains("Image")) return true;
        return false;
    }

    /**
     * 将要赋值的属性置入队列
     * 留待bot初始化完成之后赋值
     *
     * @param path              上传图片的本地路径
     * @param imageCacheConf    图片缓存时的设置
     */
    private void putImageMap(String path,ImageCacheConf imageCacheConf){
        if(BotFrameworkConfig.imageMap.containsKey(path)){
            BotFrameworkConfig.imageMap.get(path).add(imageCacheConf);
        }else {
            List<ImageCacheConf> list=new ArrayList<>();
            list.add(imageCacheConf);
            BotFrameworkConfig.imageMap.put(path,list);
        }
    }
}
