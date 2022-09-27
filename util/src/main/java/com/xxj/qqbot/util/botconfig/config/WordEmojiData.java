package com.xxj.qqbot.util.botconfig.config;

import com.xxj.qqbot.util.botconfig.functioncompent.configload.BotConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.ConfigExclude;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.ConfigKey;

import java.util.List;

@BotConfig(source = "${${basepath}.emojipath}"+"*.txt",variablePrefix = {"basepath=mirai.basepath"})
public class WordEmojiData {

    @ConfigKey(name = "wordemoji_happy")
    public static List<String> happyEmoji;

    @ConfigKey(name = "wordemoji_sad")
    public static List<String> sadEmoji;

    @ConfigKey(name = "wordemoji_mad")
    public static List<String> madEmoji;

    @ConfigKey(name = "wordemoji_despise")
    public static List<String> despiseEmoji;

    @ConfigKey(name = "wordemoji_shy")
    public static List<String> shyEmoji;

    @ConfigExclude
    public static List<String> randomEmoji;
}
