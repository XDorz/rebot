package com.xxj.qqbot.event.friendevent;

import com.xxj.qqbot.event.aop.init.BotEvents;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEvent;
import com.xxj.qqbot.util.common.BotMessageInfo;
import com.xxj.qqbot.util.common.CommonEventToolUtil;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.util.List;

@BotEvents
public class FriendEvent extends SimpleListenerHost {

    @ListenEvent(startWith = "涩图")
    public void sexPic (FriendMessageEvent event, BotMessageInfo info){
        if(!BotFrameworkConfig.rootId.equals(event.getSender().getId())) return;
        List<MessageChain> sexPicMessages = CommonEventToolUtil.getSexPic(info, false, 20);
        if(sexPicMessages==null){
            event.getSubject().sendMessage("搜索无结果，换个关键词/标签 试试吧~");
            return;
        }
        ForwardMessageBuilder builder=new ForwardMessageBuilder(event.getSender());
        builder.setDisplayStrategy(BotFrameworkConfig.forwardDisplay);
        for (MessageChain sexPicMessage : sexPicMessages) {
            builder.add(event.getSender(),sexPicMessage);
        }
        event.getSubject().sendMessage(builder.build());
    }

    @ListenEvent(startWith = "色图",autoSend = false)
    public void saxPic (MessageChainBuilder builder, FriendMessageEvent event, BotMessageInfo info) throws IOException {
        if(!BotFrameworkConfig.rootId.equals(event.getSender().getId())) return;
        List<MessageChain> sexPicMessages = CommonEventToolUtil.getSexPic(info, true,20);
        if(sexPicMessages==null){
            event.getSubject().sendMessage("搜索无结果，换个关键词/标签 试试吧~");
            return;
        }
        ForwardMessageBuilder forwardBuilder=new ForwardMessageBuilder(event.getSender());
        forwardBuilder.setDisplayStrategy(BotFrameworkConfig.forwardDisplay);
        for (MessageChain sexPicMessage : sexPicMessages) {
            forwardBuilder.add(event.getSender(),sexPicMessage);
        }
        MessageReceipt<Friend> receipt = event.getSender().sendMessage(forwardBuilder.build());
//        receipt.recallIn(BotInfo.r18RecallTime*1000);
    }
}
