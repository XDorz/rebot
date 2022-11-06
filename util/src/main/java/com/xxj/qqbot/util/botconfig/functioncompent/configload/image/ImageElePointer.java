package com.xxj.qqbot.util.botconfig.functioncompent.configload.image;

import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Field;

/**
 * 单个image信息元素
 */
@Data
@ToString
public class ImageElePointer {

    /**
     * image所在的属性
     */
    Field targetField;

    /**
     * 图片本地路径
     */
    String path;

    /**
     * 获取image对象的key
     */
    String key;

    /**
     * 在本地缓存中的key
     */
    String jsonKey;

    /**
     * 在list中的位置
     */
    Integer location;

    /**
     * #用户拓展点，为用户自己的图像做更新#
     * #也用于菜单与帮助图片的缓存更新#
     */
    String imageId;

    /**
     * 更新周期
     */
    Integer cacheDay;

    public ImageElePointer(Field targetField,String path,String key,String jsonKey,Integer location,Integer cacheDay){
        this.targetField=targetField;
        this.path=path;
        this.key=key;
        this.jsonKey=jsonKey;
        this.location=location;
        this.cacheDay=cacheDay==null? BotFrameworkConfig.cacheDay:cacheDay;
    }

    public ImageElePointer(Field targetField,String key,Integer location,String imageId,Integer cacheDay){
        this.targetField=targetField;
        this.key=key;
        this.location=location;
        this.imageId=imageId;
        this.cacheDay=cacheDay==null? BotFrameworkConfig.cacheDay:cacheDay;
    }
}
