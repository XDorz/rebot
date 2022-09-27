package com.xxj.qqbot.util.botconfig.functioncompent.configload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 一项配置中有多行
 * 不同配置分隔符为一行或者多行空白行
 * 效果同
 * @see BotConfig 中的multiLine
 * 但其优先级比他高
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiLineConfig {

    /**
     * 该配置中是否有多个行
     */
    boolean multiLine() default true;
}
