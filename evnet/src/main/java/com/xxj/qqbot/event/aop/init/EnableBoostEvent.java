package com.xxj.qqbot.event.aop.init;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Import({EventIocInject.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableBoostEvent {

    /**
     * 多个扫描包名
     */
    @AliasFor("value")
    String[] basePackages() default {};

    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * 机器人账号
     */
    String account() default "";

    /**
     * 机器人密码
     */
    String password() default "";

    /**
     * root管理qqId;
     */
    String rootId() default "";
}
