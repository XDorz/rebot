package com.xxj.qqbot.util.botconfig.functioncompent.configload;

import com.xxj.qqbot.util.botconfig.config.constant.BotConfigTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注该注解的类将被扫描并根据配置文件配置里面的属性
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.TYPE})
public @interface BotConfig {

    /**
     * 读取配置文件位置
     */
    String source();


    /**
     * 配置文件装配类型
     */
    BotConfigTypeEnum type() default BotConfigTypeEnum.DEFAULT_TYPE;

    /**
     * 作为spring环境的前缀
     */
    String[] variablePrefix() default {};

    /**
     * 标注在属性上时是否使用父类的路径
     */
    boolean[] useClassPrefix() default {};

    /**
     * 多行配置视为一项
     * 以空白行为分隔符
     * 效果同
     * @see MultiLineConfig
     * 但优先级没他高
     */
    boolean[] multiLine() default {};
}
