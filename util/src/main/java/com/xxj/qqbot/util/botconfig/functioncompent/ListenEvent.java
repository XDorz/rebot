package com.xxj.qqbot.util.botconfig.functioncompent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将标记的类的所有方法注册入监听队列
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListenEvent {

    //匹配回复功能
    /**
     * 匹配回复开头
     */
    String[] startWith() default {};

    /**
     * 匹配回复结尾
     */
    String[] endsWith() default {};

    /**
     * 匹配某段话
     */
    String[] contains() default {};

    /**
     * 匹配整句话
     */
    String[] equals() default {};

    /**
     * 正则匹配
     */
    String[] regex() default {};

    /**
     * 是否需要at
     */
    boolean[] atBot() default {};

    /**
     * 上述条件时全部满足还是满足一项
     */
    boolean satisfyAll() default false;

    /**
     * 不做任何处理
     */
    boolean[] isNative() default {};





    //帮助模块
    /**
     * 排除出主菜单
     */
    boolean menuExclude() default false;

    /**
     * 功能归类类型
     */
    String type() default "";

    /**
     * 功能帮助
     */
    String help() default "";

    /**
     * true:帮助/功能列表 以图片形式发送
     * false:以文字形式发送
     */
    boolean[] helpSendImage() default {};

    /**
     * 功能别名，开启与关闭功能时使用
     * 如果未设置别名使用黑白名单时指明功能将使用方法名
     */
    String alias() default "";




    //流程控制功能
    /**
     * 是否引用回复
     */
    boolean[] quoteReply() default {};

    /**
     * 是否翻译繁体字
     */
    boolean[] transTraditional () default {};

    /**
     * 是否需要让人等待
     */
    boolean needWait() default false;

    /**
     * 等待语句
     */
    String waitMessage() default "少女祈祷中。。。";

    /**
     * 是否自动发送
     * 即不用在方法中写消息发送语句
     * 默认需要注入MessageChainBuilder的时候使用
     */
    boolean[] autoSend() default {};

    /**
     * 该功能是否启用黑名单模式
     * 选择否将使用白名单模式
     */
    boolean[] blackListType() default {};


    /**
     * 消息撤回时间
     */
    int recallTime() default -1;

    /**
     * 是否@Sender
     */
    boolean[] atSender() default {};

    /**
     * 该消息是否需要以转发形式发送
     */
    boolean forwardMessage() default false;

    /**
     * 拓展监听事件的token
     */
    String appendToken() default "";
}
