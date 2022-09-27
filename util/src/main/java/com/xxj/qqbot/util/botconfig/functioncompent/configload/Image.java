package com.xxj.qqbot.util.botconfig.functioncompent.configload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被此注解标记的类或属性
 * 将用于缓存图片信息
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.TYPE})
public @interface Image {

    /**
     * 图片更新周期
     */
    int[] cacheDay() default {};

    /**
     * 是否启用随机数
     */
    boolean[] randomNum() default {};

    /**
     * 是否启用图片自动更新
     */
    boolean[] autoUpdate() default {};

    /**
     * 图片保存的方法
     * 是保存图片上传后的id还是保存图片的本地路径
     * IMAGE_ID_TYPE or IMAGE_PATH_TYPE
     */
    boolean[] stringSaveImagePath() default {};
}
