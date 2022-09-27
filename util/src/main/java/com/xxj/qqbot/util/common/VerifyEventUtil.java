package com.xxj.qqbot.util.common;

import com.xxj.qqbot.util.botconfig.config.BotInfo;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;

//todo 增加各种验证
public class VerifyEventUtil {

    public static boolean verifyAtBot(MessageChain chain){
        return chain.contains(new At(BotInfo.botId));
    }

}
