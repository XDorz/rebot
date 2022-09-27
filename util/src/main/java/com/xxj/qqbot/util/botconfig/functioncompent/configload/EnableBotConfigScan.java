package com.xxj.qqbot.util.botconfig.functioncompent.configload;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在启动类上
 * 开启类扫描
 * 注解装配类所在包路径
 */
@EnableScheduling
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({BotConfigScanPath.class})
public @interface EnableBotConfigScan {

    /**
     * 扫描包名
     */
    @AliasFor("value")
    String[] basePackages() default {};

    @AliasFor("basePackages")
    String[] value() default {};
}
