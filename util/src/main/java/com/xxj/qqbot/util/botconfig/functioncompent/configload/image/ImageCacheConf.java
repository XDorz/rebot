package com.xxj.qqbot.util.botconfig.functioncompent.configload.image;

import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.Image;
import com.xxj.qqbot.util.botconfig.config.constant.BotConfigTypeEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
@Getter
public class ImageCacheConf {

    /**
     * 需要注入属性的类
     */
    private Class<?> clazz;

    /**
     * 需要注入图片的属性
     */
    private Field field;

    /**
     * 图片路径
     */
    private String path;

    /**
     * 缓存最大更新周期
     */
    private Integer cacheDay;

    /**
     * 是否自动更新
     */
    private boolean update;

    /**
     * 是否使用随机周期
     */
    private boolean useRandomNum;

    /**
     * 使用什么方式存储图片
     */
    private boolean saveId;

    public void setSaveId(BotConfigTypeEnum type){
        this.saveId=saveId(type);
    }

    public void setSaveId(boolean saveId){
        this.saveId=saveId;
    }

    public ImageCacheConf(Class<?> clazz,Field field,String path){
        defaultConstructMethod(clazz,field,path);
    }

    public ImageCacheConf(Class<?> clazz,Field field,String path,boolean saveId){
        defaultConstructMethod(clazz,field,path);
        this.saveId=saveId;
    }

    public ImageCacheConf(Class<?> clazz,Field field,String path,BotConfigTypeEnum typeEnum){
        defaultConstructMethod(clazz,field,path);
        saveId=saveId(typeEnum);
    }

    public ImageCacheConf(Class<?> clazz, Field field, String path, Image config){
        this.clazz=clazz;
        this.field=field;
        this.path=path;
        if(config==null){
            cacheDay= BotFrameworkConfig.cacheDay;
            update=BotFrameworkConfig.autoImageUpdate;
            useRandomNum=BotFrameworkConfig.useRandomNum;
            saveId=BotFrameworkConfig.stringSaveImageId;
            return;
        }
        cacheDay= config.cacheDay().length==0?BotFrameworkConfig.cacheDay:config.cacheDay()[0];
        update=config.autoUpdate().length==0?BotFrameworkConfig.autoImageUpdate:config.autoUpdate()[0];
        useRandomNum=config.randomNum().length==0?BotFrameworkConfig.useRandomNum:config.randomNum()[0];
        saveId=config.stringSaveImagePath().length==0?BotFrameworkConfig.stringSaveImageId:!config.stringSaveImagePath()[0];
    }

    private static boolean saveId(BotConfigTypeEnum typeEnum){
        if(typeEnum.equals(BotConfigTypeEnum.IMAGE_PATH_TYPE)){
            return false;
        }else if(typeEnum.equals(BotConfigTypeEnum.FILE_PATH_TYPE)){
            return false;
        }else if(typeEnum.equals(BotConfigTypeEnum.IMAGE_ID_TYPE)){
            return true;
        }else {
            return BotFrameworkConfig.stringSaveImageId;
        }
    }

    /**
     * 最简构造方法
     */
    private void defaultConstructMethod(Class<?> clazz,Field field,String path){
        this.clazz=clazz;
        this.field=field;
        this.path=path;
        cacheDay= BotFrameworkConfig.cacheDay;
        update=BotFrameworkConfig.autoImageUpdate;
        useRandomNum=BotFrameworkConfig.useRandomNum;
        saveId=BotFrameworkConfig.stringSaveImageId;
    }

}
