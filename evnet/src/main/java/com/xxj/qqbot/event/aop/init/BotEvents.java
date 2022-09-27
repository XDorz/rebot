package com.xxj.qqbot.event.aop.init;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotEvents {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
