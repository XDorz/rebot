package com.xxj.qqbot.util.botconfig.functioncompent.configload.image;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.common.ImageUploadUtil;
import com.xxj.qqbot.util.common.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.Image;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AutoReloadImage {

    private List<ImageElePointer> newPointers=new ArrayList<>();

    private Map<String,ImageCacheJson> jsonMap;

    private Calendar calendar;

    private boolean needUpdate;

    private boolean hasCacheFile;

    /**
     * 定时任务，更新图片
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void reloadImage(){
        log.info("开始执行例行图片更新！");
        initComponent();
        List<ImageElePointer> pointers = ImageHolder.holder.get(ImageHolder.updateOrder);
        for (ImageElePointer pointer : pointers) {
            Field field = pointer.getTargetField();
            try {
                if(pointer.getKey()!=null){
                    //Map类
                    if(pointer.getLocation()!=null){
                        //当中有list
                        Map<String,List> tarMap = (Map<String,List>)field.get(null);
                        Class<?> type = tarMap.get(pointer.getKey()).get(pointer.getLocation()).getClass();
                        Object obj = parseVal(pointer, type);
                        tarMap.get(pointer.getKey()).set(pointer.getLocation(),obj);
                        field.set(null,tarMap);
                        putNewPointer(pointer);
                        updateCacheData(pointer,obj);
                    }else {
                        //当中没有list
                        Map<String,Object> tarMap = (Map<String,Object>)field.get(null);
                        Class<?> type = tarMap.get(pointer.getKey()).getClass();
                        Object obj = parseVal(pointer, type);
                        tarMap.put(pointer.getKey(),obj);
                        field.set(null,tarMap);
                        putNewPointer(pointer);
                        updateCacheData(pointer,obj);
                    }
                }else {
                    //List或者String
                    if(pointer.getLocation()!=null){
                        //List
                        List list=(List) field.get(null);
                        Class<?> type = list.get(pointer.getLocation()).getClass();
                        Object obj = parseVal(pointer, type);
                        list.set(pointer.getLocation(),obj);
                        putNewPointer(pointer);
                        updateCacheData(pointer,obj);
                    }else {
                        //String
                        Class<?> type = field.get(null).getClass();
                        Object obj = parseVal(pointer, type);
                        field.set(null,obj);
                        putNewPointer(pointer);
                        updateCacheData(pointer,obj);
                    }
                }
            }catch (IllegalAccessException e){
                log.error(field.getName()+"更新图片失败！！！【"+pointer.toString()+"】");
            }
        }
        ImageHolder.holder.set(ImageHolder.updateOrder,newPointers);
        newPointers=new ArrayList<>();
        if(needUpdate){
            FileUtil.writeString(JSONObject.toJSONString(jsonMap),BotFrameworkConfig.botImageCacheFile,StandardCharsets.UTF_8);
        }
        //顺序计数+1，如果已经超过了最大容量，则重置为0
        ImageHolder.updateOrder=getCycleNum(ImageHolder.updateOrder,1);
    }

    public <T> T parseVal(ImageElePointer pointer,Class<T> clazz){
        if(Image.class.isAssignableFrom(clazz)){
            if(pointer.getPath()!=null){
                return (T) ImageUploadUtil.upload(pointer.getPath());
            }else if(pointer.getImageId()!=null) {
                InputStream imageStream = ImageUtil.getStreamFromImage(pointer.getImageId());
                Image image = ImageUploadUtil.upload(imageStream);
                return (T) image;
            }
        }else if(String.class.isAssignableFrom(clazz)){
            if(pointer.getPath()!=null){
                return (T) ImageUploadUtil.uploadForId(pointer.getPath());
            } else if(pointer.getImageId()!=null){
                InputStream imageStream = ImageUtil.getStreamFromImage(pointer.getImageId());
                Image image = ImageUploadUtil.upload(imageStream);
                return (T) image.getImageId();
            }
        }
        log.error("未知的缓存类型"+clazz.getCanonicalName()+"！！！\n" +
                "可使用的缓存类型：\n" +
                "1."+ClassTypeConstant.IMAGE+"\n" +
                "2."+ClassTypeConstant.STRING+"\n");
        return null;
    }

    /**
     * 获得非相同循环数
     *
     * @param original          原来的数
     * @param addNum            增量
     * @return
     */
    private Integer getCycleNum(Integer original,Integer addNum){
        int max=ImageHolder.holder.size();
        if(max==0) return 0;
        int total=original+addNum;
        if(total>=max){
            return total-max;
        }
        return total;
    }

    /**
     * 将pointer置入新的顺序中
     */
    public void putNewPointer(ImageElePointer pointer){
        int i=getCycleNum(ImageHolder.updateOrder,pointer.getCacheDay());
        if(i==ImageHolder.updateOrder){
            newPointers.add(pointer);
        }else{
            ImageHolder.holder.get(i).add(pointer);
        }
    }

    /**
     * 更新本地图片缓存
     */
    private void updateCacheData(ImageElePointer pointer,Object obj){
        if(pointer.getJsonKey()==null){
            return;
        }
        needUpdate=true;
        String id=null;
        if(obj instanceof Image){
            id=((Image)obj).getImageId();
        }
        if(obj instanceof String){
            id=(String)obj;
        }
        if(!jsonMap.containsKey(pointer.getJsonKey())){
            if(hasCacheFile){
                log.warn("未在图像缓存文件中找到更新图片的有关信息，正在重新上传！");
            }
            jsonMap.put(pointer.getJsonKey(),new ImageCacheJson(getNewEXPDate(pointer),id,pointer.getPath()));
        }else {
            ImageCacheJson cacheJson = jsonMap.get(pointer.getJsonKey());
            cacheJson.setImageId(id);
            cacheJson.setEXPdate(getNewEXPDate(pointer));
        }
    }

    /**
     * 初始化属性
     */
    private void initComponent(){
        if(BotFrameworkConfig.botImageCacheFile!=null){
            if(!BotFrameworkConfig.botImageCacheFile.exists()){
                log.warn("未找到图像缓存文件，将重新创建！");
                hasCacheFile=false;
                if(!BotFrameworkConfig.botImageCacheFile.getParentFile().exists()){
                    BotFrameworkConfig.botImageCacheFile.getParentFile().mkdirs();
                }
                try {
                    BotFrameworkConfig.botImageCacheFile.createNewFile();
                } catch (IOException e) {
                    log.error("创建图像缓存文件失败！！！");
                }
            }else {
                hasCacheFile=true;
                //读取json内容并还原回map
                String cacheData = FileUtil.readString(BotFrameworkConfig.botImageCacheFile, StandardCharsets.UTF_8);
                jsonMap=new HashMap<>();
                JSONObject jsonObject = JSONObject.parseObject(cacheData);
                for (String key : jsonObject.keySet()) {
                    ImageCacheJson cacheJson = jsonObject.getJSONObject(key).toJavaObject(ImageCacheJson.class);
                    jsonMap.put(key,cacheJson);
                }
            }
        }
        needUpdate=false;
        calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR,4);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
    }

    /**
     * 获取新的过期时间
     */
    private Date getNewEXPDate(ImageElePointer pointer){
        Calendar clone = (Calendar)calendar.clone();
        clone.add(Calendar.DATE,pointer.getCacheDay());
        return clone.getTime();
    }
}
