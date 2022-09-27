package com.xxj.qqbot.util.botconfig.init;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.BotRegister;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.DefaultEventConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvLoad;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvPrefix;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EventDefaultConfig;
import com.xxj.qqbot.util.common.ConfigLoaderUtil;
import com.xxj.qqbot.util.common.ImageUploadUtil;
import com.xxj.qqbot.util.common.MoriBotException;
import com.xxj.qqbot.util.common.ValUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@Slf4j
@Component
public class BotFrameworkConfigLoad {

    /**
     * 将机器人初始化置于配置文件加载之后
     */
    @Autowired
    ConfigInit configInit;

    @Autowired
    Environment environment;

    private static ApplicationContext context;

    private static Pattern helpValPattern=Pattern.compile("\\$\\{[^\\{]+\\}");

    private String banedCacheFileName="banedCache";

    public static final String BLACKLIST="blackList";

    public static final String WHITELIST="whiteList";

    public static final String ADMINISTRATOR="administrator";

    public static final String GLOBALBANNED="globalBanned";

    @Autowired
    private void setContext(ApplicationContext applicationContext){
        context=applicationContext;
    }

    @PostConstruct
    public void initConfig(){
        //初始化bot事件默认配置信息
        try {
            initDefaultEventConfig();
        } catch (Exception e) {
            log.error("event设置出现未知错误，请检查自定义图片缓存是否设置正确",e);
        }
        //初始化bot信息
        if(BotFrameworkConfig.enableListenEvent){
            try {
                registerBot();
            } catch (Exception e) {
                log.error("bot实例创建失败！",e);
                System.exit(1);
            }
        }
    }

    private void registerBot() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if(!BotFrameworkConfig.boostEvent){
            return;
        }
        List<Class<? extends BotRegister>> classes = ConfigLoaderUtil.fetchConcreteClass(BotRegister.class);
        if(classes.size()!=0){
            Class<? extends BotRegister> clazz = classes.get(0);
            BotRegister botRegister = clazz.getConstructor().newInstance();
            //为里面的属性赋值
            injectFieldVal(botRegister);
            BotFrameworkConfig.bot=botRegister.registerBot();
            if(botRegister.getRootId()==null){
                log.error("管理员账号为空可能导致某些功能的不可用，如bot某些功能的开关，管理员的任命");
                BotFrameworkConfig.rootId=-1L;
            }else {
                BotFrameworkConfig.rootId=botRegister.getRootId();
            }
        }else if(BotFrameworkConfig.botId!=null&&BotFrameworkConfig.password!=null){
            BotFrameworkConfig.bot=BotRegister.registerDefaultBot(
                    BotFrameworkConfig.botId,
                    BotFrameworkConfig.password
            );
            BotFrameworkConfig.password="NULL";
        }else if(Bot.getInstances().size()!=0){
            BotFrameworkConfig.bot=Bot.getInstances().get(0);
        }else if(context.containsBean("bot")){
            BotFrameworkConfig.bot=(Bot)context.getBean("bot");
        }else {
            log.error("您使用了bot事件监听增强功能\n" +
                    "但我们并没有在上下文中找到bot对象或创建方法\n" +
                    "请从以下方法选择一个解决\n" +
                    "1.请在类扫描范围内继承BotRegister抽象类并使用自定义注册方法或使用框架默认注册方法\n" +
                    "2.在启动类的EnableBoostEvent注解上标明您的机器人账号和密码\n" +
                    "3.自己手动注册一个bot(可能因为bean加载时序问题获取不到)\n" +
                    "4.将bot作为bean并取名为'bot'交于springIoc管理(可能因为bean加载时序问题获取不到)\n" +
                    "以上方法排序同时代表推荐度与稳定度");
            System.exit(1);
        }
        BotFrameworkConfig.botId=BotFrameworkConfig.bot.getId();
        BotFrameworkConfig.workDirPath=BotFrameworkConfig.bot.getConfiguration().getWorkingDir().getAbsolutePath()+ File.separator;
        readBanedList();
        registerManageEvent();
    }

    /**
     * 注册管理员命令监听方法
     */
    private void registerManageEvent(){
        //添加管理
        BotFrameworkConfig.bot.getEventChannel().subscribeAlways(GroupMessageEvent.class,event -> {
            String context=ValUtil.getMessageContext(event.getMessage());
            boolean global=false;
            if(context.startsWith("全局")||context.startsWith("global")){
                global=true;
                context=context.replace("全局","").replace("global","").trim();
            }
            if (context.startsWith("添加管理")||context.startsWith("addAdministrator")){
                if(BotFrameworkConfig.rootId.equals(event.getSender().getId())){
                    List<At> administrator = ValUtil.getAtExceptBot(event.getMessage());
                    if(administrator!=null&&administrator.size()!=0){
                        List<Long> collect = administrator.stream()
                                .map(At::getTarget)
                                .collect(Collectors.toList());
                        writeBanedList(collect,true);
                        StringBuffer sb=new StringBuffer();
                        Group group = event.getSubject();
                        for (Long id : collect) {
                            NormalMember member = group.get(id);
                            String name = member.getNameCard();
                            if(!StringUtils.hasText(name)) name=member.getNick();
                            sb.append(name+" ");
                        }
                        event.getSubject().sendMessage("添加管理员"+sb.toString()+"成功！！！");
                    }else {
                        event.getSubject().sendMessage("用法：添加管理 @您要添加的管理员[可多个]");
                    }
                }else {
                    event.getSubject().sendMessage("非常抱歉，您无此权限！");
                    return;
                }
            }else if(context.startsWith("移除管理")||context.startsWith("删除管理")||context.startsWith("removeAdministrator")){
                if(BotFrameworkConfig.rootId.equals(event.getSender().getId())){
                    List<At> administrator = ValUtil.getAtExceptBot(event.getMessage());
                    if(administrator!=null&&administrator.size()!=0){
                        List<Long> collect = administrator.stream()
                                .map(At::getTarget)
                                .collect(Collectors.toList());
                        writeBanedList(collect,false);
                        StringBuffer sb=new StringBuffer();
                        Group group = event.getSubject();
                        for (Long id : collect) {
                            NormalMember member = group.get(id);
                            String name = member.getNameCard();
                            if(!StringUtils.hasText(name)) name=member.getNick();
                            sb.append(name+" ");
                        }
                        event.getSubject().sendMessage("移除管理员"+sb.toString()+"成功！！！");
                    }else {
                        event.getSubject().sendMessage("用法：移除管理 @您要移除的管理员[可多个]");
                    }
                }else {
                    event.getSubject().sendMessage("非常抱歉，您无此权限！");
                    return;
                }
            }else if(context.endsWith("on")||context.endsWith("ON")||context.startsWith("打开")||context.startsWith("开启")){
                Long id = event.getSender().getId();
                if(!global&&!BotFrameworkConfig.administrators.contains(id)){
                    event.getSubject().sendMessage("非常抱歉，您无权限！");
                    return;
                }
                //打开群功能
                context = context.replaceFirst("打开", "").replaceFirst("开启", "");
                context=context.replace("on","").replace("ON","").trim();
                if(!BotFrameworkConfig.blackList.containsKey(context)){
                    event.getSubject().sendMessage("我倒，没有找到要打开的功能¿?请检查输入的功能名称~");
                    return;
                }
                if(global){
                    if(!id.equals(BotFrameworkConfig.rootId)){
                        event.getSubject().sendMessage("非常抱歉，您无权限！");
                        return;
                    }
                    writeBanedList(context,false);
                    event.getSubject().sendMessage("全局「"+context+"」功能已开启！");
                    return;
                }
                Long groupId = event.getSubject().getId();
                writeBanedList(context,groupId,true);
                event.getSubject().sendMessage("「"+context+"」功能已打开！");
            }else if(context.endsWith("off")||context.endsWith("OFF")||context.startsWith("关闭")||context.startsWith("停用")){
                Long id = event.getSender().getId();
                if(!global&&!BotFrameworkConfig.administrators.contains(id)){
                    event.getSubject().sendMessage("非常抱歉，您无权限！");
                    return;
                }
                //关闭群功能
                context = context.replaceFirst("关闭", "").replaceFirst("停用", "");
                context=context.replace("off","").replace("OFF","").trim();
                if(!BotFrameworkConfig.blackList.containsKey(context)){
                    event.getSubject().sendMessage("我倒，没有找到要关闭的功能¿?请检查输入的功能名称~");
                    return;
                }
                if(global){
                    if(!id.equals(BotFrameworkConfig.rootId)){
                        event.getSubject().sendMessage("非常抱歉，您无权限！");
                        return;
                    }
                    writeBanedList(context,true);
                    event.getSubject().sendMessage("全局「"+context+"」功能已关闭！");
                    return;
                }
                Long groupId = event.getSubject().getId();
                writeBanedList(context,groupId,false);
                event.getSubject().sendMessage("「"+context+"」功能已关闭！");
            }else if(context.endsWith("--help")||context.endsWith("help")||context.endsWith("帮助")){
                //帮助菜单
                context=context.replace("--help","").replace("help","").replace("帮助","");
                Image cacheImage = BotFrameworkConfig.cacheHelperImage.get(context);
                if(cacheImage!=null){
                    event.getSubject().sendMessage(cacheImage);
                    return;
                }
                String helpVal = BotFrameworkConfig.functionHelp.get(context);
                if(helpVal==null){
                    event.getSubject().sendMessage("没有名为「"+context+"」的功能或者该功能未设置介绍");
                    return;
                }
                //todo 分析，绘制，上传
            }
        });
    }

    /**
     * 改变黑/白名单记录并持久化到文件
     *
     * @param key
     * @param groupId
     * @param isAdd
     */
    private void writeBanedList(String key,Long groupId,boolean isAdd){
        JSONObject blackJson = BotFrameworkConfig.banedList.getJSONObject(BLACKLIST);
        JSONObject whiteJson = BotFrameworkConfig.banedList.getJSONObject(WHITELIST);
        if(isAdd){
            BotFrameworkConfig.blackList.get(key).remove(groupId);
            BotFrameworkConfig.whiteList.get(key).add(groupId);

            blackJson.getJSONArray(key).remove(groupId);
            if(!whiteJson.getJSONArray(key).contains(groupId)) whiteJson.getJSONArray(key).add(groupId);
        }else {
            BotFrameworkConfig.blackList.get(key).add(groupId);
            BotFrameworkConfig.whiteList.get(key).remove(groupId);

            whiteJson.getJSONArray(key).remove(groupId);
            if(!blackJson.getJSONArray(key).contains(groupId)) blackJson.getJSONArray(key).add(groupId);
        }
        FileUtil.writeString(BotFrameworkConfig.banedList.toJSONString(),BotFrameworkConfig.bannedCacheFile,StandardCharsets.UTF_8);
    }

    private void writeBanedList(List<Long> administratorIds,boolean isAdd){
        JSONArray administrators = BotFrameworkConfig.banedList.getJSONArray(ADMINISTRATOR);
        if(isAdd){
            for (Long id : administratorIds) {
                BotFrameworkConfig.administrators.add(id);
                if(!administrators.contains(id)) administrators.add(id);
            }
        }else {
            for (Long id : administratorIds) {
                BotFrameworkConfig.administrators.remove(id);
                administrators.remove(id);
            }
        }
        FileUtil.writeString(BotFrameworkConfig.banedList.toJSONString(),BotFrameworkConfig.bannedCacheFile,StandardCharsets.UTF_8);
    }

    private void writeBanedList(String functionName,boolean isAdd){
        JSONArray globalBanned = BotFrameworkConfig.banedList.getJSONArray(GLOBALBANNED);
        if(isAdd){
            BotFrameworkConfig.globalBanned.add(functionName);
            if(!globalBanned.contains(functionName)) globalBanned.add(functionName);
        }else {
            globalBanned.remove(functionName);
            BotFrameworkConfig.globalBanned.remove(functionName);
        }
        FileUtil.writeString(BotFrameworkConfig.banedList.toJSONString(),BotFrameworkConfig.bannedCacheFile,StandardCharsets.UTF_8);
    }

    /**
     * 将黑/白名单与管理员缓存持久化写入文件
     */
    public static void WriteBanedJson(){
        BotFrameworkConfig.banedList.put(BLACKLIST,BotFrameworkConfig.blackList);
        BotFrameworkConfig.banedList.put(WHITELIST,BotFrameworkConfig.whiteList);
        BotFrameworkConfig.banedList.put(ADMINISTRATOR,BotFrameworkConfig.administrators);
        BotFrameworkConfig.banedList.put(GLOBALBANNED,BotFrameworkConfig.globalBanned);
        FileUtil.writeString(BotFrameworkConfig.banedList.toJSONString(),BotFrameworkConfig.bannedCacheFile,StandardCharsets.UTF_8);
    }

    /**
     * 从json文件中读取黑/白名单及管理员数据
     */
    private void readBanedList() {
        File file=new File(BotFrameworkConfig.workDirPath+banedCacheFileName);
        BotFrameworkConfig.bannedCacheFile=file;
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error("黑/白名单持久化文件创建失败！！",e);
                return;
            }
            BotFrameworkConfig.whiteList=new HashMap<>();
            BotFrameworkConfig.blackList=new HashMap<>();
            BotFrameworkConfig.administrators=new HashSet<>();
            BotFrameworkConfig.globalBanned=new HashSet<>();
            BotFrameworkConfig.administrators.add(BotFrameworkConfig.rootId);
            JSONObject jsonObject=new JSONObject();
            jsonObject.put(BLACKLIST,BotFrameworkConfig.blackList);
            jsonObject.put(WHITELIST,BotFrameworkConfig.whiteList);
            jsonObject.put(ADMINISTRATOR,BotFrameworkConfig.administrators);
            jsonObject.put(GLOBALBANNED,BotFrameworkConfig.globalBanned);
            BotFrameworkConfig.banedList=jsonObject;
            FileUtil.writeString(jsonObject.toJSONString(),file,StandardCharsets.UTF_8);
        }else {
            String json = FileUtil.readString(file, StandardCharsets.UTF_8);
            BotFrameworkConfig.banedList=JSONObject.parseObject(json);
            BotFrameworkConfig.blackList=BotFrameworkConfig.banedList.getJSONObject(BLACKLIST).toJavaObject(new TypeReference<Map<String,Set<Long>>>(){});
            BotFrameworkConfig.whiteList=BotFrameworkConfig.banedList.getJSONObject(WHITELIST).toJavaObject(new TypeReference<Map<String,Set<Long>>>(){});
            BotFrameworkConfig.administrators=BotFrameworkConfig.banedList.getJSONArray(ADMINISTRATOR).toJavaObject(new TypeReference<Set<Long>>(){});
            BotFrameworkConfig.globalBanned=BotFrameworkConfig.banedList.getJSONArray(GLOBALBANNED).toJavaObject(new TypeReference<Set<String>>(){});
            if(!BotFrameworkConfig.administrators.contains(BotFrameworkConfig.rootId)){
                writeBanedList(Collections.singletonList(BotFrameworkConfig.rootId),true);
            }
        }
    }

    /**
     * 解析功能帮助内容
     */
//    private static List<Image> getHelperImage(String helpVal)throws MoriBotException{
//        Matcher matcher = helpValPattern.matcher(helpVal);
//        String key=null;
//        if(matcher.find()){
//            String holderKey = matcher.group();
//            helpVal=helpVal.replace(holderKey,"");
//            holderKey=holderKey.replace("${","").replace("}","");
//            int i=-1;
//            Object val=null;
//            if((i=holderKey.indexOf('.'))!=-1){
//                val=BotFrameworkConfig.injectedFieldHolder.get(holderKey.substring(0,i));
//                key=holderKey.substring(i+1);
//            }else {
//                val=BotFrameworkConfig.injectedFieldHolder.get(holderKey);
//            }
//            //单引用情况
//            if(helpVal.trim().equals("")) return getHelperImage(val,key);
//        }else {
//            //纯文字情况
//            return getHelperImage(helpVal,null);
//        }
//        if (matcher.find()){
//            //二次引用情况，前一次为背景板，后一次为文字内容
//
//        }else {
//            //单次引用配合文字情况
//            return getHelperImage()
//        }
//        //todo 三次及以上引用不考虑
//    }

    //todo 完善helper
    private static List<Image> getHelperImage(Object obj, String key) throws MoriBotException {
        if(obj==null) throw new MoriBotException("获取的帮助为空，请检查是否为key错误或者目标属性未曾正常注入！！！");
        if(obj instanceof Map){
            if(!StringUtils.hasText(key)){
                throw new MoriBotException("所给值为map但未规定key，请使用${fieldName.keyName}来确定key");
            }
            int i=-1;
            if((i=key.indexOf('.'))!=-1){
                //map内为list情况
                String type=key.substring(i+1);
                key=key.substring(0,i);
                return getHelperImage(((Map) obj).get(key),type);
            }else {
                //map内为非集合情况
                return getHelperImage(((Map) obj).get(key),null);
            }
        }else if(obj instanceof List){
            if(!StringUtils.hasText(key)&&!key.equals("random")&&!key.equals("first")&&!key.equals("all")){
                throw new MoriBotException("所给值为list但未规定使用类型，请使用${fieldName.type}  type:[random,first,all]");
            }
            if(key.equals("random")){
                return parseImage(ValUtil.getRandomVal((List)obj));
            }else if(key.equals("first")){
                return parseImage(((List)obj).get(0));
            }else if(key.equals("all")){
                return parseImage(obj);
            }
        }else{
            return parseImage(obj);
        }
        return null;
    }

//    private static List<BufferedImage> getBackground(Object obj) throws MoriBotException{
//        //todo 待完成，获取背景板
//    }

    private static List<Image> parseImage(Object obj) throws MoriBotException{
        if(obj==null) return null;
        if(obj instanceof File){
            return Collections.singletonList(ImageUploadUtil.upload((File) obj));
        }else if(obj instanceof BufferedImage){
            return Collections.singletonList(ImageUploadUtil.upload((BufferedImage) obj));
        }else if(obj instanceof Image){
            return Collections.singletonList((Image) obj);
        }else if(obj instanceof String){
            File file=new File((String) obj);
            if(file.exists()) return Collections.singletonList(ImageUploadUtil.upload(file));

            //todo 绘制

        }else if(obj instanceof List){
            List list = (List) obj;
            List<Image> images=new ArrayList<>();
            for (Object o : list) {
                images.addAll(parseImage(o));
            }
            return images;
        }else {
            throw new MoriBotException("不支持的自定义帮助图片发送属性，要求属性类型：被配置文件加载模块注入的String(file path)，Image，BufferedImage，File 或 Map，List泛型包含上述类型的集合");
        }
        return null;
    }

    /**
     * 像Configuration注解或者Value注解一样为属性赋予springboot中环境变量值
     */
    public void injectFieldVal(Object obj){
        Class<?> clazz = obj.getClass();
        EnvPrefix envPrefix = clazz.getAnnotation(EnvPrefix.class);
        String prefix="";
        if (envPrefix!=null){
            prefix = envPrefix.prefix().equals("")?"":envPrefix.prefix()+".";
        }
        for (Field field : clazz.getDeclaredFields()){
            if(field.getName().toLowerCase().contains("log")) continue;
            boolean canAccess=field.canAccess(obj);
            field.setAccessible(true);
            EnvLoad envLoad = field.getAnnotation(EnvLoad.class);
            String name=prefix;
            try {
                if(envLoad!=null){
                    name=envLoad.usePrefix()?name+envLoad.name():envLoad.name().equals("")?field.getName():envLoad.name();
                    if(envLoad.required()){
                        field.set(obj,environment.getRequiredProperty(name,field.getType()));
                    }else {
                        field.set(obj,environment.getProperty(name,field.getType()));
                    }
                }else {
                    name=name+field.getName();
                    field.set(obj,environment.getProperty(name,field.getType()));
                }
            }catch (IllegalAccessException e){
                log.error(clazz.getName()+"---对属性"+field.getName()+"赋值未能成功，已略过对其赋值！",e);
                continue;
            }finally {
                field.setAccessible(canAccess);
            }
        }
    }


    /**
     * 初始化图像缓存设置并赋值
     */
    private static void initDefaultEventConfig() throws
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Class<? extends EventDefaultConfig>> configClasses = ConfigLoaderUtil.fetchConcreteClass(EventDefaultConfig.class);
        configClasses.remove(DefaultEventConfig.class);
        EventDefaultConfig eventConfig=null;
        if(configClasses.size()!=0){
            eventConfig=configClasses.get(0).getConstructor().newInstance();
        }else {
            eventConfig = new DefaultEventConfig();
        }
        BotFrameworkConfig.eventAtBot=eventConfig.generateAtBot();
        BotFrameworkConfig.quoteReply=eventConfig.generateQuoteReply();
        BotFrameworkConfig.autoSend=eventConfig.generateAutoSend();
        BotFrameworkConfig.atSender=eventConfig.generateAtSender();
        BotFrameworkConfig.eventNative=eventConfig.generateNative();
        BotFrameworkConfig.transTraditional=eventConfig.generateTransTraditional();
        BotFrameworkConfig.blackListType=eventConfig.generateBlackListType();
        BotFrameworkConfig.helperSendImage=eventConfig.generateHelperSendImage();
        BotFrameworkConfig.forwardDisplay=eventConfig.generateForwardDisplay();
        BotFrameworkConfig.helpImageConfig=eventConfig.generateHelpImageConfig();
        BotFrameworkConfig.menuImageConfig=eventConfig.generateMenuImageConfig();
        BotFrameworkConfig.appendListenerListenTimeMinute=eventConfig.generateAppendListenerListenTimeMinute();
    }
}
