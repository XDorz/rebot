package com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload;

import com.xxj.qqbot.util.botconfig.functioncompent.HelpImageConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.MenuImageConfig;
import lombok.Data;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.RawForwardMessage;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * botEvent事件的默认设置
 */
public interface EventDefaultConfig {

    /**
     * bot是否需要被@时触发
     */
    default boolean generateAtBot(){
        return false;
    }

    /**
     * 设置为true后除非特别设置，否则对event的处理只有参数注入
     * 不再包含其他检测等功能
     */
    default boolean generateNative(){
        return false;
    }


    /**
     * 是否翻译繁体为简体
     */
    default boolean generateTransTraditional(){
        return false;
    }

    /**
     * 机器人应答时是否引用用户的消息
     */
    default boolean generateQuoteReply(){
        return false;
    }

    /**
     * 是否自动发送
     * 即不用在方法中写消息发送语句
     * 前提是需要注入MessageChainBuilder的时候
     * 或者返回值是MessageChainBuilder或者Message及其子类
     */
    default boolean generateAutoSend(){
        return false;
    }

    /**
     * 是否要@指令发送人(仅限注入MessageChainBuilder时)
     */
    default boolean generateAtSender(){
        return false;
    }

    default ForwardMessage.DisplayStrategy generateForwardDisplay(){
        ForwardMessage.DisplayStrategy strategy = new ForwardMessage.DisplayStrategy() {
            @NotNull
            @Override
            public String generateTitle(@NotNull RawForwardMessage forward) {
                String senderName = forward.getNodeList().get(0).getSenderName();
                return senderName+"和 马化腾的聊天记录";
            }

            @NotNull
            @Override
            public String generateBrief(@NotNull RawForwardMessage forward) {
                return "查看私聊转发消息--mirai机器人支持库与mori框架提供技术支持";
            }
        };
        return strategy;
    }

    /**
     * 是否启用黑名单模式
     * 选择否将使用白名单模式
     */
    default boolean generateBlackListType(){
        return true;
    }

    /**
     * true:帮助/功能列表 以图片形式发送
     * false:以文字形式发送
     */
    default boolean generateHelperSendImage(){
        return true;
    }

    /**
     * 帮助图片设置
     */
    default HelpImageConfig generateHelpImageConfig(){
        return HelpImageConfig.DEFAULT;
    }

    /**
     * 菜单图片设置
     */
    default MenuImageConfig generateMenuImageConfig(){
        return MenuImageConfig.DEFAULT;
    }

    /**
     * 拓展监听事件默认持续监听时间
     */
    default int generateAppendListenerListenTimeMinute(){
        return 5;
    }

    /**
     * 临时文件位置
     */
    default File generateTempFile(){
        return new File(System.getProperty("user.dir")+"/tempFile");
    }

    /**
     * 框架基础监听服务配置
     */
    default BasicListen generateBasicListen(){
        return BasicListen.getInstance();
    }
    @Data
    static class BasicListen{
        boolean enableHelperListen;
        boolean enableMenuListen;
        boolean enableFunctionSwitch;
        boolean enableAdminGive;
        boolean enableMenuCache;
        boolean enableHelperCache;

        public BasicListen(){
            this.enableMenuListen=true;
            this.enableFunctionSwitch=true;
            this.enableHelperListen=true;
            this.enableAdminGive=true;
            this.enableHelperCache=true;
            this.enableMenuCache=true;
        }

        public static BasicListen getInstance(){
            return new BasicListen();
        }
    }
}
