package com.xxj.qqbot.util.common;

import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ConfigLoaderUtil{

    static final String[] IMAGETYPE={"jpg","png","jpeg","gif","bmp"};

    /**
     * 扫描所有带指定注解的类，已规定扫描包的位置
     *
     * @param target
     * @return
     */
    public static List<Class<?>> fetchAnnoClass(Class<? extends Annotation> target){
        final String[] BASEPACKAGE= BotFrameworkConfig.scanPath;
        final String SUFFIX="/**/*.class";
        //扫描注解
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Set<Class<?>> set=new HashSet<>();
        for (String basePackage : BASEPACKAGE) {
            try {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(basePackage) + SUFFIX;
                Resource[] resources = resourcePatternResolver.getResources(pattern);
                //MetadataReader 的工厂类
                MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                for (Resource resource : resources) {
                    //用于读取类信息
                    MetadataReader reader = readerfactory.getMetadataReader(resource);
                    //扫描到的class
                    String classname = reader.getClassMetadata().getClassName();
                    Class<?> clazz = Class.forName(classname);
                    //判断是否有指定注解
                    Annotation anno = clazz.getAnnotation(target);
                    if (anno != null) {
                        set.add(clazz);
                    }
                }
            }catch (ClassNotFoundException e){
                log.error("未找到class！",e);
            }catch(IOException e){
                log.error("获取类路径或者读取元数据失败！",e);
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * 扫描所有带指定注解的类，已规定扫描包的位置
     *
     * @param target
     * @return
     */
    public static <M> List<Class<? extends M>> fetchConcreteClass(Class<? extends M> target){
        final String[] BASEPACKAGE= BotFrameworkConfig.scanPath;
        final String SUFFIX="/**/*.class";
        //扫描注解
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Set<Class<? extends M>> set=new HashSet<>();
        for (String basePackage : BASEPACKAGE) {
            try {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(basePackage) + SUFFIX;
                Resource[] resources = resourcePatternResolver.getResources(pattern);
                //MetadataReader 的工厂类
                MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                for (Resource resource : resources) {
                    //用于读取类信息
                    MetadataReader reader = readerfactory.getMetadataReader(resource);
                    //扫描到的class
                    String classname = reader.getClassMetadata().getClassName();
                    Class<?> clazz = Class.forName(classname);
                    //判断是否为接口，非接口并且是target的子类则将其置入list
                    if(clazz.isInterface()|| Modifier.isAbstract(clazz.getModifiers())) continue;
                    if(target.isAssignableFrom(clazz))
                    set.add(clazz.asSubclass(target));
                }
            }catch (ClassNotFoundException e){
                log.error("未找到class！",e);
            }catch(IOException e){
                log.error("获取类路径或者读取元数据失败！",e);
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * 获取指定目录下所有的文件
     *
     * @param path
     * @return
     */
    public static List<File> fetchFile(String path,List<File> list,Boolean isImage){
        if(list==null) list=new ArrayList<>();
        if(!StringUtils.hasText(path)) return list;
        //匹配 (c:/abc)/(ab*de)(/abc.conf)
        Matcher matcher0 = Pattern.compile("^(.+?)[\\\\/]([^\\\\/]*\\*[^\\\\/]*)([\\\\/].+)$").matcher(path);
        if(matcher0.find()){
            File file=new File(matcher0.group(1));
            if(!file.exists()) return list;
            Pattern pattern = Pattern.compile(matcher0.group(2).replace(".", "\\.").replace("*", ".*"));
            for (File inFile : file.listFiles()) {
                Matcher matcher = pattern.matcher(inFile.getName());
                if(matcher.matches()){
                    fetchFile(inFile.getAbsolutePath()+matcher0.group(3),list,isImage);
                }
            }
            return list;
        }
        //匹配c:/**/(abc.conf)
        Matcher matcher1 = Pattern.compile("(^.+[\\\\/])\\*\\*[\\\\/](.*$)").matcher(path);
        if(matcher1.find()){
            File file=new File(matcher1.group(1));
            if(!file.exists()) return list;
            if(file.isDirectory()){
                for (File inFile : file.listFiles()) {
                    if(inFile.isDirectory()){
                        fetchFile(inFile.getAbsolutePath()+"/**/"+matcher1.group(2),list,isImage);
                    }
                }
                fetchFile(matcher1.group(1)+matcher1.group(2),list,isImage);
            }
            return list;
        }
        //匹配 c:/abc/(bcd.txt)
        Matcher matcher2 = Pattern.compile("^(.+?)([^\\\\/]+)$").matcher(path);
        if(matcher2.find()){
            File file=new File(matcher2.group(1));
            if(file.isDirectory()){
                Pattern pattern = Pattern.compile(matcher2.group(2).replace(".", "\\.").replace("*", ".*"));
                for (File inFile : file.listFiles()) {
                    Matcher matcher = pattern.matcher(inFile.getName());
                    if(matcher.matches()){
                        if(isImage==null||(isImage&&judgeImage(inFile))||(!isImage&&!judgeImage(inFile))){
                            list.add(inFile);
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * 读取文件中所有值
     *
     * @param files
     * @return
     */
    public static Map<String,List<Object>> getFileVal(List<File> files,boolean isMultiLine){
        Map<String,List<Object>> map=new HashMap<>();
        Pattern compile = Pattern.compile("(.+)http.?:");
        for (File file : files) {
            BufferedReader reader=null;
            FileReader fileReader=null;
            //创建文件读取流
            //读取文件，略过空白行
            //以“=”为分隔，“=”前的为map的key，后面为map的value
            //没有“=”的以文件名作为map的key
            try {
                fileReader=new FileReader(file);
                reader=new BufferedReader(fileReader);
            } catch (FileNotFoundException e) {
                log.error("无法为文件创建reader流",e);
                throw new Error("运行结束，exit 1");
            }
            StringBuilder valBuilder=new StringBuilder();
            boolean isHead=true;
            //略过#开头的文件
            try {
                //开始读取一份文件内容
                if(reader.ready()){
                    String val="";
                    String prefix=null;
                    List<Object> list=null;
                    while (reader.ready()){
                        //略过注释掉的行
                        if((val=reader.readLine().trim()).startsWith("#")){
                            continue;
                        }
                        //遇见空白行，非多行项的直接略过
                        //多行项的将原先储存的配置置入list
                        if(val.equals("")){
                            if(isMultiLine&&prefix!=null){
                                String value=valBuilder.toString();
                                if(!value.equals("")) map.get(prefix).add(value);
                                valBuilder=new StringBuilder();
                                isHead=true;
                            }
                            continue;
                        }
                        int i=-1;
                        Matcher matcher = compile.matcher(val);
                        if(matcher.find()){
                            i = matcher.group(1).indexOf('=');
                        }else {
                            i=val.indexOf('=');
                        }
                        if(i==-1||i==0){
                            //没有“=”的情况.区分prefix的值有无
                            //有则加入上一prefix的链表中
                            //无则将文件名（去除文件后缀名）作为map的key
                            if(prefix!=null){
                                if(isMultiLine){
                                    if(!isHead){
                                        valBuilder.append("\n");
                                    }
                                    valBuilder.append(val);
                                }else {
                                    map.get(prefix).add(val);
                                }
                            }else {
                                //无前缀的情况
                                prefix=file.getName().split("\\.")[0];
                                if(map.containsKey(prefix)){
                                    log.error("读取文件"+file.getName()+"时发现相同前缀："+prefix+"前值已被后值覆盖！");
                                }
                                list=new ArrayList<>();
                                map.put(prefix,list);
                                if(isMultiLine){
                                    valBuilder.append(val);
                                    isHead=false;
                                }else {
                                    list.add(val);
                                }
                            }
                        }else {
                            //含有“=”的情况，新开一个链表，存入map
                            prefix=val.substring(0,i);
                            list=new ArrayList<>();
                            String value = val.substring(i + 1);
                            if(map.containsKey(prefix)){
                                log.error("读取文件"+file.getName()+"时发现相同前缀："+prefix+"前值已被后值覆盖！");
                            }
                            map.put(prefix,list);
                            if(!value.equals("")&&isMultiLine){
                                valBuilder.append(value);
                                isHead=false;
                            }else if(!value.equals("")){
                                list.add(value);
                            }
                        }
                    }
                    String value=null;
                    if(valBuilder!=null && !(value=valBuilder.toString()).equals("") && map.containsKey(prefix)){
                        map.get(prefix).add(value);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 读取文件内容
     */
    public static Map<String,List<Object>> getFileVal(String path,boolean isMultiLine){
        List<File> files = fetchFile(path, null,null);
        return getFileVal(files,isMultiLine);
    }

    /**
     * 按名称对文件进行分类(去后缀名)
     */
    public static Map<String,List<Object>> getFileMap(List<File> files){
        Map<String,List<Object>> map=new HashMap<>();
        for (File file : files) {
            String name = file.getName();
            int i=-1;
            if((i=name.lastIndexOf("."))!=-1){
                name=name.substring(0,i);
            }
            if(map.containsKey(name)){
                map.get(name).add(file);
            }else {
                List<Object> list=new ArrayList<>();
                list.add(file);
                map.put(name,list);
            }
        }
        return map;
    }

    public static Map<String,List<Object>> getFileMap(String path){
        List<File> files = fetchFile(path, null,null);
        return getFileMap(files);
    }

    /**
     * 读取path所代表的文件，按 父文件名 为key的map返回
     */
    public static Map<String,List<File>> getParentNameSortImageFileMap(String path){
        List<File> imageFiles = fetchFile(path, null,true);
        Map<String,List<File>> map=new HashMap<>();
        List<File> list=null;
        String preName=null;
        //按父文件名分类存放file
        for (File file : imageFiles) {
            String parentName = file.getParentFile().getName();
            if(preName==null){
                preName=parentName;
                list=new ArrayList<>();
                list.add(file);
            }else {
                //判断之前的父文件名和现在得到的父文件名是否相等
                if(parentName.equals(preName)){
                    //相等则将当前文件存入list
                    list.add(file);
                } else {
                    //不相等则将已存入list的文件放入map
                    if(map.containsKey(preName)){
                        map.get(parentName).addAll(list);
                    }else {
                        map.put(preName,list);
                    }
                    //并将preName修改为现在的name，新开一个链表
                    preName=parentName;
                    list=new ArrayList<>();
                    list.add(file);
                }
            }
        }
        if(preName!=null&&list!=null){
            map.put(preName,list);
        }
        return map;
    }

    /**
     * 读取path所代表的文件，按 去后缀的文件名 为key的map返回
     */
    public static Map<String,List<File>> getFileNameSortImageFileMap(String path){
        List<File> imageFiles = fetchFile(path, null,true);
        Map<String,List<File>> map=new HashMap<>();
        for (File file : imageFiles) {
            int i=file.getName().lastIndexOf('.');
            String fileName=file.getName();
            if(i!=-1){
                fileName=fileName.substring(0,i);
            }
            if(map.containsKey(fileName)){
                map.get(fileName).add(file);
            }else {
                List<File> list=new ArrayList<>();
                list.add(file);
                map.put(fileName,list);
            }
        }
        return map;
    }

    public static boolean judgeImage(File file){
        String fileName=file.getName();
        for (String type : IMAGETYPE) {
            if(fileName.endsWith("."+type)) return true;
        }
        return false;
    }
}
