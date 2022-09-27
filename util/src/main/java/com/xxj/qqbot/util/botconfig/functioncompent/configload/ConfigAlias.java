package com.xxj.qqbot.util.botconfig.functioncompent.configload;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 内存中的配置属性别名
 * 与{@link ConfigKey}区别
 * {@link ConfigKey}读取的是配置文件中的别名
 * {@link ConfigAlias}是被注入的属性存入容器的别名
 * @see com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig#injectedFieldHolder
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigAlias {

    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";
}
