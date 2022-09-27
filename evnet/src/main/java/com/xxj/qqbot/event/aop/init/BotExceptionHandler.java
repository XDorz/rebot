package com.xxj.qqbot.event.aop.init;

import net.mamoe.mirai.event.AbstractEvent;

/**
 * bot异常处理
 * 实现该接口的类处理监听事件过程中产生的异常
 */
public interface BotExceptionHandler {

    /**
     * 异常处理
     */
    void handleException(AbstractEvent event,Throwable throwable);
}
