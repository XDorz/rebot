package com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从environment(即application.yml)中的指定字段取得值
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvLoad {

    /**
     * 从环境中读取的字段名称
     */
    String name();

    /**
     * 是否是必须的
     */
    boolean required() default false;

    /**
     * 如果有EnvPrefix的prefix，是否使用该prefix
     */
    boolean usePrefix() default true;
}
