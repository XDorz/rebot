package com.xxj.qqbot.util.botconfig.functioncompent;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解标记的事件回应不会持续监听发生的事件
 * 仅随于特定事件之后作为特定事件的拓展监听并回应特定事件后续内容
 */
@ListenEvent
@Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListenEventAppend {

    /**
     * 事件触发token
     */
    String token();

    /**
     * 下一个需要拓展的监听事件的token
     */
    String appendToken() default "";

    /**
     * 是否回复触发事件(即被append的事件)的sender
     */
    boolean replyFrontSender() default true;

    /**
     * 拓展事件监听最大持续时间
     */
    int[] maxListenTimeMinute() default {};



    //匹配回复功能
    /**
     * 匹配回复开头
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "startWith")
    String[] startWith() default {};

    /**
     * 匹配回复结尾
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "endsWith")
    String[] endsWith() default {};

    /**
     * 匹配某段话
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "contains")
    String[] contains() default {};

    /**
     * 匹配整句话
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "equals")
    String[] equals() default {};

    /**
     * 正则匹配
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "regex")
    String[] regex() default {};

    /**
     * 是否需要at
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "atBot")
    boolean[] atBot() default {};

    /**
     * 上述条件时全部满足还是满足一项
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "satisfyAll")
    boolean satisfyAll() default false;

    /**
     * 不做任何处理
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "isNative")
    boolean[] isNative() default {};

    //流程控制功能
    /**
     * 是否引用回复
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "quoteReply")
    boolean[] quoteReply() default {};

    /**
     * 是否翻译繁体字
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "transTraditional")
    boolean[] transTraditional () default {};

    /**
     * 是否需要让人等待
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "needWait")
    boolean needWait() default false;

    /**
     * 等待语句
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "waitMessage")
    String waitMessage() default "少女祈祷中。。。";

    /**
     * 是否自动发送
     * 即不用在方法中写消息发送语句
     * 默认需要注入MessageChainBuilder的时候使用
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "autoSend")
    boolean[] autoSend() default {};

    /**
     * 消息撤回时间
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "recallTime")
    int recallTime() default -1;

    /**
     * 是否@Sender
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "atSender")
    boolean[] atSender() default {};

    /**
     * 该消息是否需要以转发形式发送
     */
    @AliasFor(annotation = ListenEvent.class,attribute = "forwardMessage")
    boolean forwardMessage() default false;
}
