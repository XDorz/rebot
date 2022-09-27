package com.xxj.qqbot.util.botconfig.config;

import com.xxj.qqbot.util.botconfig.functioncompent.configload.BotConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.ConfigExclude;


@BotConfig(source = "${mirai.basepath.replyapipath}"+"*api.conf")
public class Apis {

    //涩图api
    public static String sexPicApi;
    //代理pixiv图片api
    public static String pixivProxy;


    //聊天机器人api
    public static String qinyunkeApi;
    public static String sizhiApi;
    public static String tulinApi;
    public static String moliyunApi;
    public static String moliKey;
    public static String moliSecret;
    public static String sizhiKey;
    @ConfigExclude
    public static int nowApi;
    @ConfigExclude
    public static String autoReply;


    //功能api
    public static String jokeApi;

    public static String beautyApi;

    public static String loveApi;

    public static String saoApi;

    public static String zheyanApi;

    public static String jindianApi;

    public static String duanziApi;

    public static String zuichouApi;

    public static String huangqiangApi;

    public static String yingyuApi;

    public static String shiciApi;

    public static String shunkouApi;

    public static String miyuApi;

    public static String zaoanApi;

    public static String aiqinApi;

    public static String youyuApi;

    public static String jitangApi;

    public static String fenJinApi;

    public static String dongManApi;

    public static String cosApi;

    public static String cosPlayApi;

    public static String worldApi;

    public static String taoBaoApi;

    public static String lolSkinApi;

    public static String webHotApi;

    public static String biliHotApi;

    public static String zhihuHotApi;

    public static String tiktokHotApi;

    public static String weishiHotApi;

    public static String toutiaoHotApi;

    public static String qrPicApi;

    public static String calendarApi;

    public static String bingApi;

    public static String englishApi;

    public static String translateApi;

    public static String translateAppId;

    public static String translateAppSecret;

    public static String erCiYuanApi;

    public static String cosMobileApi;

    public static String cosComputerApi;

    public static String girlComputerApi;

    public static String girlMobileApi;

    public static String kantApi;

    public static String jupaiApi;

    public static String huangliApi;

    public static String singApi;

    public static String articleApi;

    public static String threesixzeroApi;

    public static String zipaiApi;

    public static String zipai2Api;

    public static String jinxihexiApi;

    public static String dizhiApi;

    public static String datiApi;

    public static String yiqinApi;

    public static String chengyuApi;

    public static String tiangouApi;

    public static String wangzheApi;

    public static String nonliApi;

    public static String wangyiyunApi;

    public static String kugouApi;

    public static String kuwoApi;

    public static String doutuApi;

    public static String fivekApi;
}
