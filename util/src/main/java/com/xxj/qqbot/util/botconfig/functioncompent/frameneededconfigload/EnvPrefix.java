package com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 环境配置通用头，类似于
 * @see ConfigurationProperties 中的prefix属性
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvPrefix {

    String prefix();
}
