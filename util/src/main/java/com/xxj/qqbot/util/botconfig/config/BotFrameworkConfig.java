package com.xxj.qqbot.util.botconfig.config;

import com.alibaba.fastjson.JSONObject;
import com.xxj.qqbot.util.botconfig.functioncompent.HelpImageConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.MenuImageConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.image.ImageCacheConf;
import com.xxj.qqbot.util.botconfig.init.ScheduleTaskConfig;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.Image;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 机器人核心配置中心
 */
public class BotFrameworkConfig {

    //机器人相关设置
    /**
     * 机器人对象
     */
    public static Bot bot;

    /**
     * 机器人账号/id
     */
    public static Long botId;

    /**
     * 工作区路径
     */
    public static String workDirPath;

    /**
     * 管理员id
     */
    public static Long rootId;

    /**
     * 机器人密码(机器人成功登录后将会被清空)
     * 即实际运行时获取不到该值
     */
    public static String password;


    //框架配置
    /**
     * 是否开启机器人事件扫描
     */
    public static boolean enableListenEvent;

    /**
     * 类加载器需要扫描的包
     */
    public static String[] scanPath;


    //图片缓存相关设置
    /**
     * 缓存更新周期
     */
    public static Integer cacheDay;

    /**
     * 是否自动更新
     */
    public static boolean autoImageUpdate;

    /**
     * 是否使用随机周期
     */
    public static boolean useRandomNum;

    /**
     * 使用什么方式存储图片
     */
    public static boolean stringSaveImageId;

    //event增强配置
    public static boolean eventAtBot;

    public static boolean eventNative;

    public static boolean transTraditional;

    public static boolean quoteReply;

    public static boolean autoSend;

    public static boolean atSender;

    public static boolean blackListType;

    public static boolean helperSendImage;


    /**
     * 待注入的image表
     */
    public static Map<String, List<ImageCacheConf>> imageMap;

    /**
     * 图片缓存json文件
     */
    public static File botImageCacheFile;

    /**
     * 是否需要增强event
     */
    public static boolean boostEvent=false;

    /**
     * 黑名单
     */
    public static Map<String, Set<Long>> blackList;

    /**
     * 白名单
     */
    public static Map<String,Set<Long>> whiteList;

    /**
     * 配置读取模块注入的属性的容器
     */
    public static Map<String,Field> injectedFieldHolder=new HashMap<>();

    /**
     * 主菜单功能列表
     */
    public static Map<String,List<String>> menu=new HashMap<>();

    /**
     * 功能帮助
     */
    public static Map<String,String> functionHelp=new HashMap<>();

    /**
     * 采用懒加载模式，不进行持久化缓存
     */
    public static Map<String, Image> cacheHelperImage=new HashMap<>();

    /**
     * 管理员ids
     */
    public static Set<Long> administrators;

    /**
     * 黑白名单持久化json
     */
    public static JSONObject banedList;

    /**
     * 全局禁止,仅限root使用,采用黑名单机制
     */
    public static Set<String> globalBanned;

    /**
     * 黑白名单与管理员持久化缓存文件
     */
    public static File bannedCacheFile;

    /**
     * 定时任务线程池
     */
    public static Map<String, ScheduledFuture> scheduledTaskMap= ScheduleTaskConfig.futureMap;

    /**
     * 转发消息风格
     */
    public static ForwardMessage.DisplayStrategy forwardDisplay;

    /**
     * 帮助/主菜单 图片配置
     */
    public static HelpImageConfig helpImageConfig;

    /**
     * 主菜单 图片配置
     */
    public static MenuImageConfig menuImageConfig;

    /**
     * 拓展事件监听表
     */
    public static Map<String, Method> appendEvent=new HashMap<>();

    /**
     * 拓展监听方法map
     */
    public static Map<String, Listener<? extends AbstractEvent>> appendEventListener=new ConcurrentHashMap<>();

    /**
     * 拓展监听事件默认持续监听时间
     */
    public static int appendListenerListenTimeMinute;
}
