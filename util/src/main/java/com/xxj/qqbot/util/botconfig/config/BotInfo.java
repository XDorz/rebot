package com.xxj.qqbot.util.botconfig.config;

import com.xxj.qqbot.util.botconfig.functioncompent.configload.BotConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.ConfigKey;

import java.io.File;
import java.util.List;

@BotConfig(source = "${botpath}"+"bot.conf",variablePrefix = {"botpath=mirai.basepath.botpath"})
public class BotInfo {

    @ConfigKey(name = "account")
    @BotConfig(source = "${botpath}"+"account.txt")
    public static Long botId;

    public static String botName;

    public static String bname;

    public static String verifyAddFriend;

    @BotConfig(source = "${botpath}"+"account.txt")
    public static Long account;

    @BotConfig(source = "${botpath}"+"account.txt")
    public static String password;

    @BotConfig(source = "${botpath}"+"account.txt")
    public static Long rootId;

    @BotConfig(source = "${botpath}"+"account.txt")
    public static String heartBeatingType;

    @BotConfig(source = "${botpath}"+"group.conf")
    public static List<Long> listeningGroups;

    @BotConfig(source = "${botpath}"+"sexGroup.conf")
    public static List<Long> sexGroups;

    @BotConfig(source = "${botpath}"+"manager.conf")
    public static List<Long> managers;

    public static Integer r18RecallTime;

    public static Integer maxPic;

    public static Integer sendSpacing;

    public static Integer picCacheDay;

    public static Integer gameRecallTime;

    public static Integer cosRecallTime;
    /**
     * 百度合成语音语速
     */
    public static String voicespd;

    /**
     * 百度合成语音音调
     */
    public static String voicepit;
    /**
     * 百度合成语音音量
     */
    public static String voicevol;

    /**
     * 百度合成语音发音人
     */
    public static String voiceper;

    /**
     * 临时文件
     */
    public static File tempFile;
}
