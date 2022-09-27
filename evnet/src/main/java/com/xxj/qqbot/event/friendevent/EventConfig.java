package com.xxj.qqbot.event.friendevent;

import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvPrefix;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EventDefaultConfig;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.RawForwardMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EnvPrefix(prefix = "mirai.config.event")
public class EventConfig implements EventDefaultConfig {

    private static boolean quoteReply;

    private static boolean autoSend;

    private static boolean transTraditional;

    @Override
    public boolean generateQuoteReply() {
        return quoteReply;
    }

    @Override
    public boolean generateAutoSend() {
        return autoSend;
    }

    @Override
    public boolean generateTransTraditional() {
        return transTraditional;
    }

    @Override
    public ForwardMessage.DisplayStrategy generateForwardDisplay() {
        ForwardMessage.DisplayStrategy strategy = new ForwardMessage.DisplayStrategy() {
            @NotNull
            @Override
            public String generateTitle(@NotNull RawForwardMessage forward) {
                String senderName = forward.getNodeList().get(0).getSenderName();
                return senderName+"的聊天记录";
            }

            @NotNull
            @Override
            public String generateBrief(@NotNull RawForwardMessage forward) {
                return "「精彩内容，敬请查看!」";
            }

            @NotNull
            @Override
            public String generateSummary(@NotNull RawForwardMessage forward) {
                return "♡查看"+forward.getNodeList().size()+"条私聊转发消息♡";
            }
        };
        return strategy;
    }
}
