package com.xxj.qqbot.util.botconfig.functioncompent.configload;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被此注解标记的类或属性
 * 其string字段将会保存文件的绝对路径
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.TYPE})
public @interface PathUrl {

    /**
     * 是否是记录文件绝对路径的属性
     */
    @AliasFor("value")
    boolean saveFilePath() default true;

    @AliasFor("saveFilePath")
    boolean value() default true;
}
