package com.xxj.qqbot.util.botconfig.init;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.image.*;
import com.xxj.qqbot.util.common.ImageUploadUtil;
import com.xxj.qqbot.util.common.ConfigLoaderUtil;
import com.xxj.qqbot.util.common.MoriException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class ImageInit {

    /**
     * 将图片初始化延后至机器人初始化之后
     */
    @Autowired
    BotFrameworkConfigLoad botFrameworkConfigLoad;

    private String cacheFileName="imageCacheData";

    private Map<String,ImageCacheJson> jsonMap;

    private Calendar calendar;

    private static Random random;

    private boolean hasCacheFile;

    private boolean needUpdate;

    private boolean dayNeedReduce;

    @PostConstruct
    public void init(){
        log.info("开始从本地缓存读取或上传图像文件");
        String cacheFilePath = BotFrameworkConfig.workDirPath + cacheFileName;
        File cacheFile = new File(cacheFilePath);
        initComponent(cacheFile);
        for (String key : BotFrameworkConfig.imageMap.keySet()) {
            //获取图片的缓存设置
            List<ImageCacheConf> confs = BotFrameworkConfig.imageMap.get(key);
            //获取属性对应路径的所有图片
            Map<String, List<File>> imageNameMap = ConfigLoaderUtil.getFileNameSortImageFileMap(key);
            Map<String, List<File>> parentNameMap = ConfigLoaderUtil.getParentNameSortImageFileMap(key);
            for (ImageCacheConf conf : confs) {
                Field field = conf.getField();
                //map类型和其他类型分开处理
                if(Map.class.isAssignableFrom(field.getType())){
                    suitImageHolder(conf.getCacheDay());
                    Map map=new HashMap<>();
                    if(field.getGenericType().getTypeName().contains(ClassTypeConstant.LIST)){
                        //处理Map<String,List>类型
                        //使用以父文件为key的map
                        if(field.getGenericType().getTypeName().contains(ClassTypeConstant.BUFFEREDIMAGE)){
                            for (String k : parentNameMap.keySet()) {
                                List<File> files=parentNameMap.get(k);
                                List<BufferedImage> list=new ArrayList<>();
                                for (File file : files) {
                                    try {
                                        list.add(ImageIO.read(file));
                                    } catch (IOException e) {
                                        log.error(conf.getClazz().getName()+"---"+conf.getField().getName()+"---读取图像文件"+file.getAbsolutePath()+"出错！",e);
                                    }
                                }
                                map.put(k,list);
                            }
                        }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.IMAGE)){
                            for (String k : parentNameMap.keySet()) {
                                String jsonKey=conf.getClass().getName() + "_" + field.getName()+"_"+k;
                                List<File> files=parentNameMap.get(k);
                                List<Image> list=new ArrayList<>();
                                int i=0;
                                for (File file : files) {
                                    String cacheJsonKey=jsonKey+file.getName();
                                    Bridge bridge=fetchId(file,cacheJsonKey,conf);
                                    list.add(Image.fromId(bridge.getId()));
                                    if(conf.isUpdate()){
                                        ImageHolder.holder.get(bridge.getSaveDay()).add(
                                                new ImageElePointer(field,file.getAbsolutePath(),k,cacheJsonKey,i,conf.getCacheDay()));
                                    }
                                    i++;
                                }
                                map.put(k,list);
                            }
                        }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.LIST+"<"+ClassTypeConstant.STRING+">")){
                            if(conf.isSaveId()){
                                for (String k : parentNameMap.keySet()) {
                                    String jsonKey=conf.getClass().getName() + "_" + field.getName()+"_"+k;
                                    List<File> files=parentNameMap.get(k);
                                    List<String> list=new ArrayList<>();
                                    int i=0;
                                    for (File file : files) {
                                        String cacheJsonKey=jsonKey+file.getName();
                                        Bridge bridge=fetchId(file,cacheJsonKey,conf);
                                        list.add(bridge.getId());
                                        if(conf.isUpdate()){
                                            ImageHolder.holder.get(bridge.getSaveDay()).add(
                                                    new ImageElePointer(field,file.getAbsolutePath(),k,cacheJsonKey,i,conf.getCacheDay()));
                                        }
                                        i++;
                                    }
                                    map.put(k,list);
                                }
                            }else {
                                for (String k : parentNameMap.keySet()) {
                                    List<File> files=parentNameMap.get(k);
                                    List<String> list=new ArrayList<>();
                                    for (File file : files) {
                                        list.add(file.getAbsolutePath());
                                    }
                                    map.put(k,list);
                                }
                            }
                        }else {
                            log.error(conf.getClazz().getName()+"---"+conf.getField().getName()+
                                    "---map<String,List<?>>型图像属性注入只适用于value值泛型为String，Image，BufferedImage时");
                        }
                        try {
                            field.set(null,map);
                        } catch (IllegalAccessException e) {
                            log.error(conf.getClazz().getName()+"---"+conf.getField().getName()+
                                    "---无法为该属性赋值，它是否被非public所修饰？");
                        }
                    }else {
                        //处理Map<String,?>类型
                        //使用以文件名为key的map
                        if(field.getGenericType().getTypeName().contains(ClassTypeConstant.BUFFEREDIMAGE)){
                            for (String k : imageNameMap.keySet()) {
                                File file=imageNameMap.get(k).get(0);
                                try {
                                    map.put(k,ImageIO.read(file));
                                } catch (IOException e) {
                                    log.error(conf.getClazz().getName()+"---"+conf.getField().getName()+"---读取图像文件"+file.getAbsolutePath()+"出错！",e);
                                }
                            }
                        }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.IMAGE)){
                            for (String k : imageNameMap.keySet()) {
                                String jsonKey=conf.getClass().getName() + "_" + field.getName()+"_"+k;
                                File file=imageNameMap.get(k).get(0);
                                Bridge bridge=fetchId(file,jsonKey,conf);
                                map.put(k,Image.fromId(bridge.getId()));
                                if(conf.isUpdate()){
                                    ImageHolder.holder.get(bridge.getSaveDay()).add(
                                            new ImageElePointer(field,file.getAbsolutePath(),k,jsonKey,null,conf.getCacheDay()));
                                }
                            }
                        }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.STRING+">")){
                            if(conf.isSaveId()){
                                for (String k : imageNameMap.keySet()) {
                                    String jsonKey=conf.getClass().getName() + "_" + field.getName()+"_"+k;
                                    File file=imageNameMap.get(k).get(0);
                                    Bridge bridge=fetchId(file,jsonKey,conf);
                                    map.put(k,bridge.getId());
                                    if(conf.isUpdate()){
                                        ImageHolder.holder.get(bridge.getSaveDay()).add(
                                                new ImageElePointer(field,file.getAbsolutePath(),k,jsonKey,null,conf.getCacheDay()));
                                    }
                                }
                            }else {
                                for (String k : imageNameMap.keySet()) {
                                    File file=imageNameMap.get(k).get(0);
                                    map.put(k,file.getAbsoluteFile());
                                }
                            }
                        }else {
                            log.error(conf.getClazz().getName()+"---"+conf.getField().getName()+
                                    "---map<String,?>型图像属性注入只适用于value值为String，Image，BufferedImage时");
                        }
                        try {
                            field.set(null,map);
                        } catch (IllegalAccessException e) {
                            log.error(conf.getClazz().getName()+"---"+conf.getField().getName()+"---无法为该属性赋值，它是否被非public所修饰？");
                        }
                    }
                }else {
                    List<File> fileList=null;
                    if((fileList=imageNameMap.get(field.getName()))==null){
                        if(imageNameMap.size()==1){
                            fileList=imageNameMap.get(imageNameMap.keySet().toArray()[0]);
                        }else if((fileList=parentNameMap.get(field.getName()))==null){
                            if(parentNameMap.size()==1){
                                fileList=imageNameMap.get(parentNameMap.keySet().toArray()[0]);
                            }
                        }
                    }
                    if(fileList==null){
                        log.error(conf.getClazz().getName()+"---"+field.getName()+"--未在对应"+key+"中找到对应图片，请检查\n" +
                                "1.路径有错\n" +
                                "2.属性的名称是否所要注入的图片的父文件名\n" +
                                "3.属性的名称是否所要注入的图片的去格式后缀文件名");
                        continue;
                    }
                    try {
                        setImageValue(field,fileList,conf);
                    }catch (IllegalAccessException|MoriException e){
                        log.error(conf.getClazz().getName()+"---"+field.getName()+"---"+field.getGenericType()+"---"+e.getMessage());
                    }
                }
            }
        }
        if(needUpdate){
            JSONObject jsonObject=new JSONObject();
            jsonObject.putAll(jsonMap);
            FileUtil.writeString(jsonObject.toJSONString(),cacheFile,StandardCharsets.UTF_8);
        }
    }

    /**
     * 为image类设置属性值
     *
     * @param field                     需要赋值的属性
     * @param value                     从本地扫描到的图像文件
     * @param conf                      属性赋值配置
     * @throws IllegalAccessException
     * @throws MoriException
     */
    @SuppressWarnings("unchecked")
    private void setImageValue(Field field, List<File> value, ImageCacheConf conf) throws IllegalAccessException,MoriException{
        suitImageHolder(conf.getCacheDay());
        String key=conf.getClass().getName() + "_" + field.getName();
        if(Integer.class.isAssignableFrom(field.getType())){
            throw new MoriException(conf.getClazz().getName()+"---"+conf.getField().getName()+"---需注入Image的属性不能为Integer类");
        }else if(Long.class.isAssignableFrom(field.getType())){
            throw new MoriException(conf.getClazz().getName()+"---"+conf.getField().getName()+"---需注入Image的属性不能为Long类");
        }else if(String.class.isAssignableFrom(field.getType())){
            assertOne(value);
            File file = value.get(0);
            if(conf.isSaveId()){
                Bridge bridge=fetchId(file,key,conf);
                field.set(null,bridge.getId());
                if(conf.isUpdate()){
                    ImageHolder.holder.get(bridge.getSaveDay()).add(
                            new ImageElePointer(field,file.getAbsolutePath(),null,key,null,conf.getCacheDay()));
                }
            }else {
                String path = file.getAbsolutePath();
                field.set(null,path);
            }
        }else if(Image.class.isAssignableFrom(field.getType())){
            assertOne(value);
            File file = value.get(0);
            Bridge bridge=fetchId(file,key,conf);
            field.set(null,Image.fromId(bridge.getId()));
            if(conf.isUpdate()){
                ImageHolder.holder.get(bridge.getSaveDay()).add(
                        new ImageElePointer(field,file.getAbsolutePath(),null,key,null,conf.getCacheDay()));
            }
        }else if(BufferedImage.class.isAssignableFrom(field.getType())){
            assertOne(value);
            try {
                BufferedImage image = ImageIO.read(value.get(0));
                field.set(null,image);
            } catch (IOException e) {
                log.error(conf.getClazz().getName()+"---"+conf.getField().getName()+"---读取图像文件"+value.get(0).getAbsolutePath()+"出错！",e);
                return;
            }
        }else if(List.class.isAssignableFrom(field.getType())){
            List list=new ArrayList<String>();
            if(field.getGenericType().getTypeName().contains(ClassTypeConstant.INTEGER)){
                throw new MoriException(conf.getClazz().getName()+"---"+conf.getField().getName()+"---需注入Image的属性不能为Integer类");
            }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.LONG)){
                throw new MoriException(conf.getClazz().getName()+"---"+conf.getField().getName()+"---需注入Image的属性不能为Long类");
            }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.BUFFEREDIMAGE)){
                value.forEach(file -> {
                    try {
                        BufferedImage image = ImageIO.read(value.get(0));
                        list.add(image);
                    } catch (IOException e) {
                        log.error(conf.getClazz().getName()+"---"+conf.getField().getName()+"---读取图像文件"+value.get(0).getAbsolutePath()+"出错！",e);
                    }
                });
            }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.IMAGE)){
                int i=0;
                for (File file : value) {
                    String cacheJsonKey=key+file.getName();
                    Bridge bridge=fetchId(file,cacheJsonKey,conf);
                    list.add(Image.fromId(bridge.getId()));
                    if(conf.isUpdate()){
                        ImageHolder.holder.get(bridge.getSaveDay()).add(
                                new ImageElePointer(field,file.getAbsolutePath(),null,cacheJsonKey,i,conf.getCacheDay()));
                    }
                    i++;
                }
            }else if(field.getGenericType().getTypeName().contains(ClassTypeConstant.STRING)){
                if(conf.isSaveId()){
                    int i=0;
                    for (File file : value) {
                        String cacheJsonKey=key+file.getName();
                        Bridge bridge=fetchId(file,cacheJsonKey,conf);
                        list.add(bridge.getId());
                        if(conf.isUpdate()){
                            ImageHolder.holder.get(bridge.getSaveDay()).add(
                                    new ImageElePointer(field,file.getAbsolutePath(),null,cacheJsonKey,i,conf.getCacheDay()));
                        }
                        i++;
                    }
                }else {
                    for (File file : value) {
                        String path = file.getAbsolutePath();
                        list.add(path);
                    }
                }
            }
            field.set(null,list);
        }else {
            throw new MoriException("无法解决的属性类型："+field.getGenericType().getTypeName());
        }
    }

    /**
     * 初始化所需的各项属性
     */
    private void initComponent(File cacheFile){
        if(ImageHolder.holder==null){
            ImageHolder.holder=new ArrayList<>();
        }
        if(ImageHolder.updateOrder==null){
            ImageHolder.updateOrder=0;
        }
        random=new Random(System.currentTimeMillis());
        jsonMap=new HashMap<>();
        needUpdate=false;
        Calendar instance = Calendar.getInstance();
        if(instance.get(Calendar.HOUR)<4) dayNeedReduce=true;
        calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR,4);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        BotFrameworkConfig.botImageCacheFile=cacheFile;
        hasCacheFile=false;
        if(cacheFile.exists()){
            //读取json内容并还原回map
            String cacheData = FileUtil.readString(cacheFile, StandardCharsets.UTF_8);
            jsonMap=new HashMap<>();
            JSONObject jsonObject = JSONObject.parseObject(cacheData);
            for (String key : jsonObject.keySet()) {
                ImageCacheJson cacheJson = jsonObject.getJSONObject(key).toJavaObject(ImageCacheJson.class);
                jsonMap.put(key,cacheJson);
            }
            hasCacheFile=true;
        }else {
            try {
                cacheFile.createNewFile();
                needUpdate=true;
            } catch (IOException e) {
                log.error("创建图片缓存文件失败！",e);
            }
        }
    }

    /**
     * 判断list内元素是否只有一个
     *
     * @param list                  待检验的链表
     * @throws MoriException        属性注入数量不对等
     */
    private static void assertOne(List<?> list)throws MoriException {
        if(list.size()>1){
            throw new MoriException("该属性对应配置文件中的参数有"+list.size()+"个，类中属性仅要求一个！");
        }
        if(list.size()==0){
            throw new MoriException("该属性要求的配置文件中没有对应参数！");
        }
    }

    /**
     * 获取该张图片应该更新的周期
     *
     * @param maxDay                更新最大天数
     * @param useRandomNum          是否启用随机数
     * @param cache                 图片缓存文件
     * @return
     */
    private Integer getCacheDay(Integer maxDay,boolean useRandomNum,ImageCacheJson cache){
        if(hasCacheFile&&cache!=null){
            Date date=new Date();
            if(date.before(cache.getEXPdate())){
                int cacheDay = (int) ((cache.getEXPdate().getTime() - date.getTime()) / 86400000) + 1;
                //为了防止图片上传时在凌晨4点前又正好随机到最大值
                //重启程序后读取缓存文件获得的天数为最大天数+1
                if(cacheDay==maxDay+1){
                    return maxDay;
                }
                return cacheDay;
            }
        }
        if(useRandomNum){
            return random.nextInt(maxDay)+1;
        }
        return maxDay;
    }

    /**
     * 缓存运行时数据到本地以便下次快速启动
     *
     * @param key               className+"_"+fieldName组合的key
     * @param calendar          图片到期日期
     * @param imageId           图片id，可为null
     * @param imagePath         图片本地地址
     */
    private void setJsonMapVal(String key,Calendar calendar,String imageId,String imagePath){
        needUpdate=true;
        if(jsonMap.containsKey(key)){
            String olderPath = jsonMap.get(key).getPath();
            log.error("存在同属性下使用同名图像文件,新扫描图像已被略过！\n" +
                    "旧文件路径："+olderPath+"\n" +
                    "新文件路径："+imagePath);
        }else {
            jsonMap.put(key,new ImageCacheJson(calendar.getTime(),imageId,imagePath));
        }
    }

    /**
     * 缓存运行时数据到本地以便下次快速启动
     *
     * @param key               className+"_"+fieldName组合的key
     * @param calendar          图片到期日期
     * @param imageId           图片id，可为null
     * @param iamgePath
     */
    private void updateJsonMapVal(String key,Calendar calendar,String imageId,String iamgePath){
        needUpdate=true;
        jsonMap.put(key,new ImageCacheJson(calendar.getTime(),imageId,iamgePath));
    }

    /**
     * 获取过期时间
     *
     * @param addDay            要加上的时间
     * @return
     */
    private Calendar getCalendar(Integer addDay){
        Calendar clone = (Calendar) calendar.clone();
        clone.add(Calendar.DATE,addDay);
        return clone;
    }

    /**
     * 让容器大小适合最大更新天数
     */
    private void suitImageHolder(Integer max){
        if(max>ImageHolder.holder.size()){
            ImageHolder.holder.add(new ArrayList<>());
            suitImageHolder(max);
        }
    }

    /**
     * 读取id值
     *
     * @param file                  图像本地文件
     * @param key                   在本地缓存中的key
     * @param conf                  缓存设置
     * @return
     */
    private Bridge fetchId(File file,String key,ImageCacheConf conf){
        String id;
        ImageCacheJson cache=jsonMap.get(key);
        Integer days=getCacheDay(conf.getCacheDay(),conf.isUseRandomNum(),cache);
        //如果有缓存文件
        if(hasCacheFile&&cache!=null){
            //缓存未过期
            if(new Date().before(cache.getEXPdate())){
                id=cache.getImageId();
            }else {
                id= ImageUploadUtil.uploadForId(file);
                updateJsonMapVal(key,getCalendar(days),id,file.getAbsolutePath());
            }
        }else {
            id= ImageUploadUtil.uploadForId(file);
            days=getCacheDay(conf.getCacheDay(),conf.isUseRandomNum(),null);
            setJsonMapVal(key,getCalendar(days),id,file.getAbsolutePath());
        }
        return new Bridge(id,days-1);
    }

    /**
     * 作为承载消息的桥梁
     */
    @Getter
    @AllArgsConstructor
    private class Bridge{

        /**
         * image的id
         */
        private String id;

        /**
         * image缓存的天数
         */
        private Integer saveDay;
    }
}
