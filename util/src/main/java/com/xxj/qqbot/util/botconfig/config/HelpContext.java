package com.xxj.qqbot.util.botconfig.config;

import com.xxj.qqbot.util.botconfig.config.constant.EnvNameConstant;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.BotConfig;

import java.util.Map;

@BotConfig(source = EnvNameConstant.REPLY_DATA_PATH,multiLine = true,useClassPrefix = true)
public class HelpContext {

//    public static String yunshiHelp;
//    public static String chouxiangHelp;
//    public static String fiveK;

    @BotConfig(source = "commonHelp.txt")
    public static Map<String,String> commonHelp;

    @BotConfig(source = "toolHelp.txt")
    public static Map<String,String> toolHelp;

    @BotConfig(source = "sexHelp.txt")
    public static Map<String,String> sexHelp;
}

