package com.xxj.qqbot.event.event;

import com.xxj.qqbot.util.botconfig.config.BackgroundSource;
import com.xxj.qqbot.util.botconfig.functioncompent.HelpImageConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.MenuImageConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvLoad;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvPrefix;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EventDefaultConfig;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.RawForwardMessage;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.File;

@EnvPrefix(prefix = "mirai.config.event")
public class EventConfig implements EventDefaultConfig {

    private static boolean quoteReply;

    private static boolean autoSend;

    private static boolean transTraditional;

    @EnvLoad(usePrefix = false,name = "mirai.basepath.tempfile")
    private static String tempFilePath;

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
    public File generateTempFile() {
        return new File(tempFilePath);
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

    @Override
    public HelpImageConfig generateHelpImageConfig() {
        HelpImageConfig imageConfig = HelpImageConfig.getInstance();
        imageConfig.setAutoHeight(true);
        imageConfig.setHelpBackground(BackgroundSource.helpBkg);
        imageConfig.setHelpFont("楷体");
        imageConfig.setTitleFontSize(28);
        return imageConfig;
    }

    @Override
    public MenuImageConfig generateMenuImageConfig() {
        MenuImageConfig imageConfig=MenuImageConfig.getInstance();
        imageConfig.setMenuBackground(BackgroundSource.menuBkg);
        imageConfig.setMenuValFont("楷体");
        imageConfig.setDefaultTitleFontColor(new Color(254,67,101));
        imageConfig.setTitleColSpace(3);
        return imageConfig;
    }

    @Override
    public BasicListen generateBasicListen() {
        BasicListen basicListen = BasicListen.getInstance();
        basicListen.setEnableMenuCache(true);
        basicListen.setEnableHelperCache(true);
        return basicListen;
    }
}
