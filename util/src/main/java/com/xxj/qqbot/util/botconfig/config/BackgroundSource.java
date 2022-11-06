package com.xxj.qqbot.util.botconfig.config;

import com.xxj.qqbot.util.botconfig.config.constant.EnvNameConstant;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.BotConfig;

import java.io.File;
import java.util.List;

@BotConfig(source = EnvNameConstant.BAKGROUND_PATH)
public class BackgroundSource {

    @BotConfig(source = "menu/*.png",useClassPrefix = true)
    public static List<File> menuBkg;

    @BotConfig(source = "help/*.png",useClassPrefix = true)
    public static List<File> helpBkg;

    @BotConfig(source = "special/helpSpecial.png",useClassPrefix = true)
    public static File helpSpecial;
}
