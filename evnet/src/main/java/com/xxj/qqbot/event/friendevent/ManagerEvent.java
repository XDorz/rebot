package com.xxj.qqbot.event.friendevent;

import com.xxj.qqbot.event.aop.init.BotEvents;
import com.xxj.qqbot.event.aop.init.BotExceptionHandler;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.config.BotInfo;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEvent;
import com.xxj.qqbot.util.common.ValUtil;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.BotJoinGroupEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.UUID;

@BotEvents
public class ManagerEvent implements BotExceptionHandler {

    /**
     * 好友添加请求
     */
    @ListenEvent(isNative = true)
    public void addFriend (NewFriendRequestEvent event){
        if(event.getMessage().equals(BotInfo.verifyAddFriend)){
            event.accept();
        }
    }

    /**
     * 邀请入群请求
     */
    @ListenEvent(isNative = true)
    public void addGroup (BotInvitedJoinGroupRequestEvent event, MessageChainBuilder builder){
        builder.append("收到一则邀请入群通知：\n" +
                "邀请人id:"+event.getInvitorId()+"\n" +
                "邀请人名称:"+event.getInvitor().getNick()+"\n" +
                "邀请入群号:"+event.getGroupId()+"\n" +
                "邀请入群名:"+event.getGroupName()+"\n" +
                "是否加入？是/否/yes/no/y/n\n");
        BotFrameworkConfig.bot.getFriend(BotFrameworkConfig.rootId).sendMessage(builder.build());
        String uuid=UUID.randomUUID().toString();
        Listener<FriendMessageEvent> listener = BotFrameworkConfig.bot.getEventChannel().subscribeAlways(FriendMessageEvent.class, friendEvent -> {
            if (BotFrameworkConfig.rootId.equals(friendEvent.getSender().getId())) {
                String context = ValUtil.getMessageContext(friendEvent.getMessage());
                if (context.equals("是") || context.equalsIgnoreCase("yes") || context.equalsIgnoreCase("y")) {
                    event.accept();
                    BotFrameworkConfig.appendEventListener.get(uuid).complete();
                    BotFrameworkConfig.appendEventListener.remove(uuid);
                } else if (context.equals("否") || context.equalsIgnoreCase("no") || context.equalsIgnoreCase("n")) {
                    event.ignore();
                    BotFrameworkConfig.appendEventListener.get(uuid).complete();
                    BotFrameworkConfig.appendEventListener.remove(uuid);
                }
            }
        });
        BotFrameworkConfig.appendEventListener.put(uuid,listener);
    }

    @Override
    public void handleException(AbstractEvent event, Throwable throwable) {
        if(event instanceof MessageEvent){
            ((MessageEvent)event).getSubject().sendMessage(throwable.getMessage());
        }
    }
}