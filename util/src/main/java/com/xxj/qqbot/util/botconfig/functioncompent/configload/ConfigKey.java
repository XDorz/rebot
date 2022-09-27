package com.xxj.qqbot.util.botconfig.functioncompent.configload;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从配置文件读取的map中获取指定的key的值注入
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigKey {

    /**
     * key名称
     *
     * @return
     */
    String name();
}
