package com.xxj.qqbot.util.music;

import com.xxj.qqbot.util.botconfig.config.constant.EnvNameConstant;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.BotConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.ConfigExclude;

@BotConfig(source = EnvNameConstant.REPLY_API_PATH+"musicapi.conf")
public class MusicUrls {

    @ConfigExclude
    public static int listMaxNum=10;

    public static String bili;

    public static String qq;
}
