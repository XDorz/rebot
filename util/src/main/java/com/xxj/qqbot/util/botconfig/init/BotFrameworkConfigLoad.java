package com.xxj.qqbot.util.botconfig.init;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.image.AutoReloadImage;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.image.ImageElePointer;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.BotRegister;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.DefaultEventConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvLoad;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvPrefix;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EventDefaultConfig;
import com.xxj.qqbot.util.common.ConfigLoaderUtil;
import com.xxj.qqbot.util.common.ImageUploadUtil;
import com.xxj.qqbot.util.common.ImageUtil;
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
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
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
     * ???????????????????????????????????????????????????
     */
    @Autowired
    ConfigInit configInit;

    @Autowired
    Environment environment;

    @Autowired
    AutoReloadImage autoReloadImage;

    private static ApplicationContext context;

    private static Pattern helpValPattern=Pattern.compile("\\$\\{[^\\{]+\\}");

    private String banedCacheFileName="banedCache";

    public static final String BLACKLIST="blackList";

    public static final String WHITELIST="whiteList";

    public static final String ADMINISTRATOR="administrator";

    public static final String GLOBALBANNED="globalBanned";

    public static final String MENUADMIN="ADMINISTRATOR_";
    public static final String MENUNORMAL="NORMAL_";

    @Autowired
    private void setContext(ApplicationContext applicationContext){
        context=applicationContext;
    }

    @PostConstruct
    public void initConfig(){
        //?????????bot????????????????????????
        try {
            initDefaultEventConfig();
        } catch (Exception e) {
            log.error("event???????????????????????????????????????????????????????????????????????????",e);
        }
        //?????????bot??????
        if(BotFrameworkConfig.enableListenEvent){
            try {
                registerBot();
            } catch (Exception e) {
                log.error("bot?????????????????????",e);
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
            //????????????????????????
            injectFieldVal(botRegister);
            BotFrameworkConfig.bot=botRegister.registerBot();
            if(botRegister.getRootId()==null){
                log.error("???????????????????????????????????????????????????????????????bot??????????????????????????????????????????");
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
            log.error("????????????bot????????????????????????\n" +
                    "???????????????????????????????????????bot?????????????????????\n" +
                    "????????????????????????????????????\n" +
                    "1.??????????????????????????????BotRegister????????????????????????????????????????????????????????????????????????\n" +
                    "2.???????????????EnableBoostEvent?????????????????????????????????????????????\n" +
                    "3.????????????????????????bot(????????????bean??????????????????????????????)\n" +
                    "4.???bot??????bean????????????'bot'??????springIoc??????(????????????bean??????????????????????????????)\n" +
                    "???????????????????????????????????????????????????");
            System.exit(1);
        }
        BotFrameworkConfig.botId=BotFrameworkConfig.bot.getId();
        BotFrameworkConfig.workDirPath=BotFrameworkConfig.bot.getConfiguration().getWorkingDir().getAbsolutePath()+ File.separator;
        readBanedList();
        registerManageEvent();
    }

    /**
     * ?????????????????????????????????
     */
    private void registerManageEvent(){
        //????????????
        BotFrameworkConfig.bot.getEventChannel().subscribeAlways(GroupMessageEvent.class,event -> {
            String context=ValUtil.getMessageContext(event.getMessage());
            boolean global=false;
            if(context.startsWith("?????? ")||context.startsWith("global ")){
                global=true;
                context=context.replace("?????? ","").replace("global ","").trim();
            }
            if (context.startsWith("????????????")||context.startsWith("addAdministrator")){
                if(!BotFrameworkConfig.basicListenConfig.isEnableAdminGive()){
                    return;
                }
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
                        event.getSubject().sendMessage("???????????????"+sb.toString()+"???????????????");
                    }else {
                        event.getSubject().sendMessage("????????????????????? @????????????????????????[?????????]");
                    }
                }else {
                    event.getSubject().sendMessage("?????????????????????????????????");
                    return;
                }
            }else if(context.startsWith("????????????")||context.startsWith("????????????")||context.startsWith("removeAdministrator")){
                if(!BotFrameworkConfig.basicListenConfig.isEnableAdminGive()){
                    return;
                }
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
                        event.getSubject().sendMessage("???????????????"+sb.toString()+"???????????????");
                    }else {
                        event.getSubject().sendMessage("????????????????????? @????????????????????????[?????????]");
                    }
                }else {
                    event.getSubject().sendMessage("?????????????????????????????????");
                    return;
                }
            }else if(context.endsWith(" on")||context.endsWith(" ON")||context.startsWith("?????? ")||context.startsWith("?????? ")){
                if(!BotFrameworkConfig.basicListenConfig.isEnableFunctionSwitch()){
                    return;
                }
                Long id = event.getSender().getId();
                if(!global&&!BotFrameworkConfig.administrators.contains(id)){
                    event.getSubject().sendMessage("??????????????????????????????");
                    return;
                }
                //???????????????
                context = context.replaceFirst("?????? ", "").replaceFirst("?????? ", "");
                context=context.replace(" on","").replace(" ON","").trim();
                if(!BotFrameworkConfig.blackList.containsKey(context)){
                    event.getSubject().sendMessage("????????????????????????????????????????????????????????????????????????~");
                    return;
                }
                if(global){
                    if(!id.equals(BotFrameworkConfig.rootId)){
                        event.getSubject().sendMessage("??????????????????????????????");
                        return;
                    }
                    writeBanedList(context,false);
                    event.getSubject().sendMessage("?????????"+context+"?????????????????????");
                    return;
                }
                Long groupId = event.getSubject().getId();
                writeBanedList(context,groupId,true);
                event.getSubject().sendMessage("???"+context+"?????????????????????");
            }else if(context.endsWith(" off")||context.endsWith(" OFF")||context.startsWith("?????? ")||context.startsWith("?????? ")){
                if(!BotFrameworkConfig.basicListenConfig.isEnableFunctionSwitch()){
                    return;
                }
                Long id = event.getSender().getId();
                if(!global&&!BotFrameworkConfig.administrators.contains(id)){
                    event.getSubject().sendMessage("??????????????????????????????");
                    return;
                }
                //???????????????
                context = context.replaceFirst("?????? ", "").replaceFirst("?????? ", "");
                context=context.replace(" off","").replace(" OFF","").trim();
                if(!BotFrameworkConfig.blackList.containsKey(context)){
                    event.getSubject().sendMessage("????????????????????????????????????????????????????????????????????????~");
                    return;
                }
                if(global){
                    if(!id.equals(BotFrameworkConfig.rootId)){
                        event.getSubject().sendMessage("??????????????????????????????");
                        return;
                    }
                    writeBanedList(context,true);
                    event.getSubject().sendMessage("?????????"+context+"?????????????????????");
                    return;
                }
                Long groupId = event.getSubject().getId();
                writeBanedList(context,groupId,false);
                event.getSubject().sendMessage("???"+context+"?????????????????????");
            }else if(context.endsWith(" --help")||context.endsWith(" help")||context.endsWith(" ??????")||context.startsWith("?????? ")){
                if(!BotFrameworkConfig.basicListenConfig.isEnableHelperListen()){
                    return;
                }
                //????????????
                context=context.replace("--help","").replace("help","").replace("??????","").trim();
                String helpVal = BotFrameworkConfig.functionHelp.get(context);
                if(!StringUtils.hasText(helpVal)){
                    event.getSubject().sendMessage("???????????????"+context+"??????????????????????????????????????????");
                    return;
                }
                List<Image> images=null;
                if(BotFrameworkConfig.basicListenConfig.isEnableHelperCache()){
                    if(BotFrameworkConfig.cacheHelperImage.get(context)==null){
                        images=getHelperImage(helpVal);
                        BotFrameworkConfig.cacheHelperImage.put(context,images);
                        for (int i = 0; i < images.size(); i++) {
                            try {
                                ImageElePointer pointer=new ImageElePointer(BotFrameworkConfig.class.getField("cacheHelperImage"),
                                        context,i,images.get(i).getImageId(),null);
                                autoReloadImage.putNewPointer(pointer);
                            } catch (NoSuchFieldException e) {
                                log.error("??????????????????????????????????????????????????????field???????????????????????????????????????????????????????????????",e);
                            }
                        }
                    }else {
                        images=BotFrameworkConfig.cacheHelperImage.get(context);
                    }
                }else {
                    images=getHelperImage(helpVal);
                }
                MessageChainBuilder builder=new MessageChainBuilder();
                if(images==null || images.size()<1){
                    event.getSubject().sendMessage("??????"+context+"???????????????????????????");
                    return;
                }
                for (Image image : images) {
                    builder.append(image);
                    builder.append(new PlainText("\n"));
                }
                event.getSubject().sendMessage(builder.build());
            }else if(context.equalsIgnoreCase("menu")||context.equals("??????")||context.equals("??????")||context.equalsIgnoreCase("function")){
                if(!BotFrameworkConfig.basicListenConfig.isEnableMenuListen()){
                    return;
                }
                Long id = event.getSender().getId();
                Long groupId=event.getSubject().getId();
                boolean isComplete=BotFrameworkConfig.administrators.contains(id);
                Set<String> keySet = BotFrameworkConfig.menu.keySet();
                Image menuImage=null;
                try {
                    if(BotFrameworkConfig.basicListenConfig.isEnableMenuCache()){
                        if(isComplete){
                            if((menuImage=BotFrameworkConfig.menuCache.get(MENUADMIN+groupId))==null){
                                menuImage=ImageUploadUtil.upload(
                                        ImageUtil.drawMenu(
                                            getFunctionMenu(groupId,isComplete,keySet),
                                            BotFrameworkConfig.menuImageConfig,
                                            ImageIO.read(ValUtil.getRandomVal(BotFrameworkConfig.menuImageConfig.getMenuBackground())
                                        )));
                                BotFrameworkConfig.menuCache.put(MENUADMIN+groupId,menuImage);
                            }
                        }else{
                            if((menuImage=BotFrameworkConfig.menuCache.get(MENUNORMAL+groupId))==null){
                                menuImage=ImageUploadUtil.upload(
                                        ImageUtil.drawMenu(
                                                getFunctionMenu(groupId,isComplete,keySet),
                                                BotFrameworkConfig.menuImageConfig,
                                                ImageIO.read(ValUtil.getRandomVal(BotFrameworkConfig.menuImageConfig.getMenuBackground())
                                                )));
                                BotFrameworkConfig.menuCache.put(MENUNORMAL+groupId,menuImage);
                            }
                        }
                    }else {
                        menuImage=ImageUploadUtil.upload(
                                ImageUtil.drawMenu(
                                        getFunctionMenu(groupId,isComplete,keySet),
                                        BotFrameworkConfig.menuImageConfig,
                                        ImageIO.read(ValUtil.getRandomVal(BotFrameworkConfig.menuImageConfig.getMenuBackground())
                                        )));
                    }
                    event.getSubject().sendMessage(menuImage);
                }catch (IOException e){
                    throw new MoriBotException("????????????????????????????????????????????????",e);
                }
            }else if(context.equalsIgnoreCase("update menu") || context.equalsIgnoreCase("update all menu") ||
                    context.equals("????????????")||context.equals("??????????????????")){
                if(!BotFrameworkConfig.basicListenConfig.isEnableMenuCache()){
                    return;
                }
                Set<String> keySet = BotFrameworkConfig.menu.keySet();
                try {
                    Collection<Group> deals=new ArrayList<>();
                    if(context.contains("all") || context.contains("??????")){
                        long id = event.getSender().getId();
                        if(!BotFrameworkConfig.rootId.equals(id)){
                            event.getSubject().sendMessage("???????????????????????????????????????");
                            return;
                        }
                        deals=BotFrameworkConfig.bot.getGroups();
                    }else {
                        deals=Collections.singleton(event.getSubject());
                    }
                    for (Group group : deals) {
                        Long groupId=group.getId();
                        Image adminMenu = ImageUploadUtil.upload(
                                ImageUtil.drawMenu(
                                        getFunctionMenu(groupId, true, keySet),
                                        BotFrameworkConfig.menuImageConfig,
                                        ImageIO.read(ValUtil.getRandomVal(BotFrameworkConfig.menuImageConfig.getMenuBackground())
                                        )));
                        Image normalMenu = ImageUploadUtil.upload(
                                ImageUtil.drawMenu(
                                        getFunctionMenu(groupId, true, keySet),
                                        BotFrameworkConfig.menuImageConfig,
                                        ImageIO.read(ValUtil.getRandomVal(BotFrameworkConfig.menuImageConfig.getMenuBackground())
                                        )));
                        BotFrameworkConfig.menuCache.put(MENUADMIN+groupId,adminMenu);
                        BotFrameworkConfig.menuCache.put(MENUNORMAL+groupId,normalMenu);
                    }
                    event.getSubject().sendMessage("??????????????????");
                }catch (IOException e){
                    throw new MoriBotException("????????????????????????????????????????????????",e);
                }
            }
        });

        //todo ???friendEvent?????????????????????
    }

    public static Map<String,List<String>> getFunctionMenu(Long groupId,boolean completeShow,Set<String> keySet){
        Map<String,List<String>> functionMenu=new HashMap<>();
        for (String s : keySet) {
            List<String> functions=new ArrayList<>();
            for (String name : BotFrameworkConfig.menu.get(s)) {
                boolean banned=false;
                boolean globalBanned=false;
                if(BotFrameworkConfig.globalBanned.contains(name)){
                    globalBanned=true;
                }else {
                    if(BotFrameworkConfig.blackListName.contains(name)){
                        banned=BotFrameworkConfig.blackList.get(name).contains(groupId);
                    }else {
                        banned=!BotFrameworkConfig.whiteList.get(name).contains(groupId);
                    }
                }
                if(globalBanned || banned){
                    if(globalBanned){
                        name="("+ImageUtil.globalBanned+"){"+name+"}";
                    }else if(banned){
                        name="("+ImageUtil.banned+"){"+name+"}";
                    }
                    if(completeShow){
                        functions.add(name);
                    }
                }else {
                    functions.add(name);
                }
            }
            if(functions.size()>0){
                functionMenu.put(s,functions);
            }
        }
        return functionMenu;
    }

    /**
     * ?????????/????????????????????????????????????
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
     * ??????/????????????????????????????????????????????????
     */
    public static void WriteBanedJson(){
        BotFrameworkConfig.banedList.put(BLACKLIST,BotFrameworkConfig.blackList);
        BotFrameworkConfig.banedList.put(WHITELIST,BotFrameworkConfig.whiteList);
        BotFrameworkConfig.banedList.put(ADMINISTRATOR,BotFrameworkConfig.administrators);
        BotFrameworkConfig.banedList.put(GLOBALBANNED,BotFrameworkConfig.globalBanned);
        FileUtil.writeString(BotFrameworkConfig.banedList.toJSONString(),BotFrameworkConfig.bannedCacheFile,StandardCharsets.UTF_8);
    }

    /**
     * ???json??????????????????/???????????????????????????
     */
    private void readBanedList() {
        File file=new File(BotFrameworkConfig.workDirPath+banedCacheFileName);
        BotFrameworkConfig.bannedCacheFile=file;
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error("???/??????????????????????????????????????????",e);
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
     * ????????????????????????
     */
    private static List<Image> getHelperImage(String helpVal)throws MoriBotException{
        Matcher matcher = helpValPattern.matcher(helpVal);
        String key=null;
        if(matcher.find()){
            String holderKey = matcher.group();
            helpVal=helpVal.replace(holderKey,"").trim();
            holderKey=holderKey.replace("${","").replace("}","");
            int i=-1;
            Object val=null;
            if((i=holderKey.indexOf('%'))!=-1){
                val=BotFrameworkConfig.injectedFieldHolder.get(holderKey.substring(0,i));
                key=holderKey.substring(i+1);
            }else {
                val=BotFrameworkConfig.injectedFieldHolder.get(holderKey);
            }
            if(val==null){
                val=holderKey;
            }else {
                try {
                    val=((Field)val).get(null);
                } catch (IllegalAccessException e) {
                    throw new MoriBotException("????????????help???????????????????????????",e);
                }
            }
            Object helperObj = getHelperObj(val, key);
            if (matcher.find()){
                //???????????????????????????//????????????????????????
                String backgroundKey = matcher.group();
                helpVal=helpVal.replace(backgroundKey,"").trim();
                backgroundKey=backgroundKey.replace("${","").replace("}","");
                int j=-1;
                Object bval=null;
                String bkey=null;
                if((j=backgroundKey.indexOf('%'))!=-1){
                    bval=BotFrameworkConfig.injectedFieldHolder.get(backgroundKey.substring(0,j));
                    bkey=backgroundKey.substring(j+1);
                }else {
                    bval=BotFrameworkConfig.injectedFieldHolder.get(backgroundKey);
                }
                if(bval==null){
                    bval=backgroundKey;
                }else {
                    try {
                        bval=((Field)bval).get(null);
                    } catch (IllegalAccessException e) {
                        throw new MoriBotException("????????????help???????????????????????????",e);
                    }
                }
                Object backgroundObj = getHelperObj(bval, bkey);
                if(!StringUtils.hasText(helpVal)){
                    return parseImage(helperObj,getBackground(backgroundObj));
                }
                return parseImage(helperObj+helpVal,getBackground(backgroundObj));
            }else {
                //???????????????
                if(!StringUtils.hasText(helpVal)){
                    return parseImage(helperObj,null);
                }
                return parseImage(helperObj+helpVal,null);
            }
        }else {
            //???????????????
            Object helperObj = getHelperObj(helpVal, null);
            return parseImage(helperObj,null);
        }

        //???????????????????????????,???????????????
    }

    private static Object getHelperObj(Object obj, String key) throws MoriBotException {
        if(obj==null) throw new MoriBotException("???????????????/????????? ???????????????????????????key???????????????????????????????????????????????????");
        if(obj instanceof Map){
            if(!StringUtils.hasText(key)){
                throw new MoriBotException("????????????map????????????key????????????${fieldName%keyName}?????????key");
            }
            int i=-1;
            if((i=key.indexOf('%'))!=-1){
                //map??????list??????
                String innerKey=key.substring(i+1);
                key=key.substring(0,i);
                return getHelperObj(((Map) obj).get(key),innerKey);
            }else {
                //map?????????????????????
                return getHelperObj(((Map) obj).get(key),null);
            }
        }else if(obj instanceof List){
            if(key==null) { return ((List)obj).get(0);}
            if(!StringUtils.hasText(key) && !key.equalsIgnoreCase("random")
                    && !key.equalsIgnoreCase("first") && !key.equalsIgnoreCase("all")){
                throw new MoriBotException("????????????list????????????????????????????????????${fieldName%type}  type:[random,first,all]");
            }
            if(key.equalsIgnoreCase("random")){
                return ValUtil.getRandomVal((List)obj);
            }else if(key.equalsIgnoreCase("first")){
                return ((List)obj).get(0);
            }else if(key.equalsIgnoreCase("all")){
                return obj;
            }
        }else{
            return obj;
        }
        return null;
    }

    private static List<BufferedImage> getBackground(Object obj){
        if(obj==null) return null;
        if(obj instanceof File){
            File fobj = (File) obj;
            if(!fobj.exists()) {
                throw new MoriBotException("?????????????????????????????????????????????");
            }
            try {
                return Collections.singletonList(ImageIO.read((File) obj));
            }catch (IOException e){
                throw new MoriBotException("???????????????????????????",e);
            }
        }else if(obj instanceof BufferedImage){
            return Collections.singletonList((BufferedImage) obj);
        }else if(obj instanceof String){
            File file=new File(obj.toString());
            if(file.exists()){
                try{
                    return Collections.singletonList(ImageIO.read(file));
                }catch (IOException e){
                    throw new MoriBotException("???????????????????????????",e);
                }
            }
        }else if(obj instanceof List){
            List list = (List) obj;
            List<BufferedImage> images=new ArrayList<>();
            for (Object o : list) {
                images.addAll(getBackground(o));
            }
            return images;
        }else {
            throw new MoriBotException("?????????????????????????????????File,BufferedImage,FileUrl,?????????????????????List???");
        }
        return null;
    }

    private static List<Image> parseImage(Object obj,List<BufferedImage> backgrounds) throws MoriBotException{
        if(obj==null) return null;
        if(obj instanceof File){
            return Collections.singletonList(ImageUploadUtil.upload((File) obj));
        }else if(obj instanceof BufferedImage){
            return Collections.singletonList(ImageUploadUtil.upload((BufferedImage) obj));
        }else if(obj instanceof Image){
            return Collections.singletonList((Image) obj);
        }else if(obj instanceof String){
            File file=new File(obj.toString());
            if(file.exists()) {
                return Collections.singletonList(ImageUploadUtil.upload(file));
            }
            try{
                return Collections.singletonList(ImageUploadUtil.upload(ImageUtil.drawHelp(obj.toString(),
                        BotFrameworkConfig.helpImageConfig,
                        backgrounds==null?
                                ImageIO.read(ValUtil.getRandomVal(BotFrameworkConfig.helpImageConfig.getHelpBackground())):
                                ValUtil.getRandomVal(backgrounds))));
            }catch (IOException e){
                throw new MoriBotException("????????????????????????????????????????????????",e);
            }
        }else if(obj instanceof List){
            List list = (List) obj;
            List<Image> images=new ArrayList<>();
            for (Object o : list) {
                images.addAll(parseImage(o,backgrounds));
            }
            return images;
        }else {
            throw new MoriBotException("?????????????????????????????????File,BufferedImage,FileUrl,?????????????????? ??? Map???List????????????????????????????????????");
        }
    }

    /**
     * ???Configuration????????????Value???????????????????????????springboot??????????????????
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
                log.error(clazz.getName()+"---?????????"+field.getName()+"?????????????????????????????????????????????",e);
                continue;
            }finally {
                field.setAccessible(canAccess);
            }
        }
    }


    /**
     * ??????????????????
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
        BotFrameworkConfig.basicListenConfig=eventConfig.generateBasicListen();
        BotFrameworkConfig.appendListenerListenTimeMinute=eventConfig.generateAppendListenerListenTimeMinute();
        File file = eventConfig.generateTempFile();
        if(!file.exists()){
            file.mkdirs();
        }
        BotFrameworkConfig.tempFile=file;
    }
}
