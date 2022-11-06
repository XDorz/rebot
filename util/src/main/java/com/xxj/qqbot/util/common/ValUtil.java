package com.xxj.qqbot.util.common;

import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEvent;
import net.mamoe.mirai.message.data.*;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//WARN:此工具类依赖框架记录的的botId
//todo 完善内容获取机制
public class ValUtil {

    private static Pattern pattern=Pattern.compile("\\[[^\\[]*?\\]");

    private static Pattern atPattern=Pattern.compile("@\\d+[ ]?");

    private static Pattern numPattern=Pattern.compile("\\d+");

    private static Pattern splitPattern=Pattern.compile("[:=：∶]");

    private static Random random=new Random(System.currentTimeMillis());

    public static BotMessageInfo getMessageInfo(MessageChain chain,String context){
        BotMessageInfo messageInfo=new BotMessageInfo();
        if(context!=null){
            messageInfo.setMessageContext(context);
        }else {
            messageInfo.setMessageContext(getMessageContext(chain));
        }
        //对message中的参数赋值
        String messageContext=messageInfo.getMessageContext();
        Map<String, String> paramMap = messageInfo.getParams();
        if(messageContext!=null&&!messageContext.equals("")){
            String[] params = messageContext.split(" ");
            paramMap.put("command",params[0]);
            int j=0;
            String key=null;
            boolean singleParam=false;
            for (int i = 1; i < params.length; i++) {
                String val = params[i];
                if(i==1){
                    Matcher numMatcher = numPattern.matcher(val);
                    if(numMatcher.matches()){
                        singleParam=true;
                        paramMap.put("param"+i,val);
                        continue;
                    }
                }
                if(singleParam){
                    paramMap.put("param"+i,val);
                    continue;
                }
                Matcher splitMatcher = splitPattern.matcher(val);
                if (splitMatcher.find()){
                    paramMap.put(val.substring(0,splitMatcher.start()),val.substring(splitMatcher.start()+1));
                    continue;
                }
                Matcher numMatcher = numPattern.matcher(val);
                if(!numMatcher.find()){
                    if(key!=null){
                        j++;
                        paramMap.put("param"+j,key);
                        j++;
                        key=null;
                        paramMap.put("param"+j,val);
                    }else {
                        key=val;
                    }
                }else {
                    if(key!=null){
                        paramMap.put(key,val);
                        key=null;
                    }else {
                        j++;
                        paramMap.put("param"+j,val);
                    }
                }
            }
            if(key!=null){
                j++;
                paramMap.put("param"+j,key);
            }
        }
        List<Image> images=new ArrayList<>();
        List<At> ats=new ArrayList<>();
        List<Face> faces=new ArrayList<>();
        for (SingleMessage message : chain) {
            if(message instanceof MusicShare){
                messageInfo.setMusicShare((MusicShare)message);
                continue;
            }
            if(message instanceof ServiceMessage){
                messageInfo.setAppShare((ServiceMessage)message);
                continue;
            }
            if(message instanceof Audio){
                messageInfo.setAudio((Audio)message);
            }
            if(message instanceof Image){
                images.add((Image)message);
                continue;
            }
            if(message instanceof At){
                At at=(At)message;
                if(BotFrameworkConfig.botId==at.getTarget()){
                    messageInfo.setAtBot(at);
                }else{
                    ats.add(at);
                }
            }
            if(message instanceof Face){
                faces.add((Face)message);
            }
            if(message instanceof ForwardMessage){
                messageInfo.setForwardMessage((ForwardMessage)message);
            }
            if(message instanceof AtAll){
                messageInfo.setAtAll((AtAll)message);
            }
        }
        if(images.size()!=0) messageInfo.setImages(images.toArray(Image[]::new));
        if(ats.size()!=0) messageInfo.setAts(ats.toArray(At[]::new));
        if(faces.size()!=0) messageInfo.setEmojis(faces.toArray(Face[]::new));
        return messageInfo;
    }


    public static String getMessageContext(MessageChain chain){
        String content = chain.contentToString();
        content=deleteAt(content);
        return replaceContext(content).trim();
    }

    private static String replaceContext(String context){
        Matcher matcher = pattern.matcher(context);
        while (matcher.find()){
            context=matcher.replaceFirst("");
            matcher=pattern.matcher(context);
        }
        return context;
    }

    private static String deleteAt(String context){
        Matcher matcher = atPattern.matcher(context);
        while (matcher.find()){
            context=matcher.replaceFirst("");
            matcher=atPattern.matcher(context);
        }
        return context;
    }

    public static List<At> getAtExceptBot(MessageChain chain){
        List<At> list=new ArrayList<>();
        for (SingleMessage message : chain) {
            if(message instanceof At){
                if(((At) message).getTarget()!=BotFrameworkConfig.botId) list.add((At)message);
            }
        }
        return list;
    }

    public static String getName(Method method){
        ListenEvent listenEvent = method.getAnnotation(ListenEvent.class);
        if(StringUtils.hasText(listenEvent.alias())){
            return listenEvent.alias();
        }
        if(listenEvent.startWith().length!=0){
            return listenEvent.startWith()[0];
        }
        if(listenEvent.equals().length!=0){
            return listenEvent.equals()[0];
        }
        if(listenEvent.contains().length!=0){
            return listenEvent.contains()[0];
        }
        return method.getName();
    }


    //======================================================================================
    public static<T> T getRandomVal(List<T> list){
        int index = random.nextInt(list.size());
        return list.get(index);
    }
}
