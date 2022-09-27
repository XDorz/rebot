package com.xxj.qqbot.util.botconfig.config;

import com.xxj.qqbot.util.botconfig.config.constant.EnvNameConstant;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.BotConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.ConfigExclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

@BotConfig(source = "")
public class Yunshi {

    @BotConfig(source = EnvNameConstant.GENSHIN+"reflect.txt")
    public static Map<String,String>  genshinReflect;

    @BotConfig(source = EnvNameConstant.GENSHIN+"nameReflect.txt")
    public static Map<String,String>  genshinNameReflect;

    @BotConfig(source = EnvNameConstant.GENSHIN+"board/*.*")
    public static Map<String, BufferedImage> genshinBoard;

    @BotConfig(source = EnvNameConstant.GENSHIN+"basic/*.*")
    public static List<File> genshinBasic;

    @BotConfig(source = EnvNameConstant.YUNSHI+"font/*.*")
    public static Map<String, Font> genshinFont;

    @BotConfig(source = EnvNameConstant.GENSHIN+"title/*.*")
    public static BufferedImage genshinTitle;

    @BotConfig(source = EnvNameConstant.YUNSHI+"board/*.*")
    public static BufferedImage board;

    @BotConfig(source = EnvNameConstant.YUNSHI+"fortune/fortune.txt")
    public static File fortune;

    @ConfigExclude
    public static List<Fortune> fortunes;

    @Data
    @AllArgsConstructor
    public static class Fortune{

        private String luck;

        private String content;
    }
}