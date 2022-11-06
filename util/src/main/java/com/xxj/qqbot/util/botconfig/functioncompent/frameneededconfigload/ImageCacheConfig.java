package com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload;

import com.xxj.qqbot.util.botconfig.functioncompent.configload.EnableBotConfigScan;

import java.util.concurrent.TimeUnit;

/**
 * 设置图片缓存配置
 * 依赖于@EnableBotConfigScan
 * @see EnableBotConfigScan
 */
public interface ImageCacheConfig {

    /**
     * 图片更新周期
     */
    default int generateCacheDay(){
        return 7;
    }

    /**
     * 是否使用redis记录
     */
    default boolean generateUseRedis(){
        return true;
    }

    /**
     * 缓存更新周期单位
     */
   default TimeUnit generateTimeUnit(){
        return TimeUnit.DAYS;
   }

    /**
     * 是否开启随机更新时间
     * 随机更新可以把同一时间上传的一批图片的更新时间错开
     * 缓解服务器上传图片压力
     */
   default boolean generateRandomNum(){
       return true;
   }

    /**
     * 是否设置图片id自动更新
     * 仅限由配置文件读取的图片
     */
   default boolean generateUpdate(){
       return true;
   }

    /**
     * 图片以什么形式存储,id还是path？
     */
    default boolean stringSaveImagePath(){
        return false;
    }
}
