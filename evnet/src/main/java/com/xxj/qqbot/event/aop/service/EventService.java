package com.xxj.qqbot.event.aop.service;

import com.luhuiguo.chinese.Converter;
import com.xxj.qqbot.event.aop.init.BotExceptionHandler;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEvent;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEventAppend;
import com.xxj.qqbot.util.common.BotMessageInfo;
import com.xxj.qqbot.util.common.MoriBotException;
import com.xxj.qqbot.util.common.ValUtil;
import com.xxj.qqbot.util.common.VerifyEventUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Order(3)
@Component
@Scope("prototype")
public class EventService {

    @Pointcut("execution(* com.xxj.qqbot.event..*(..)) && (@annotation(com.xxj.qqbot.util.botconfig.functioncompent.ListenEvent)||@annotation(com.xxj.qqbot.util.botconfig.functioncompent.ListenEventAppend))")
    public void  tar(){}

    private static Pattern skipMsgPattern=Pattern.compile("( on$| ON$| off$| OFF$|^?????? |^?????? |^?????? |^?????? |^?????? |^global |help$| ??????$|^?????? )");

    @Autowired
    ApplicationContext context;

    @Autowired
    ThreadPoolTaskScheduler scheduled;

    /**
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("tar()")
    public Object eventHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        Method joinMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        ListenEvent info = AnnotatedElementUtils.getMergedAnnotation(joinMethod, ListenEvent.class);
        ListenEventAppend append = AnnotatedElementUtils.getMergedAnnotation(joinMethod, ListenEventAppend.class);
        MessageEvent event=null;
        MessageChainBuilder builder=null;
        BotMessageInfo messageInfo=null;
        for (Object obj : joinPoint.getArgs()) {
            if(obj instanceof MessageEvent){
                event=(MessageEvent) obj;
            }else if(obj instanceof BotMessageInfo){
                messageInfo=(BotMessageInfo) obj;
            } else if(obj instanceof MessageChainBuilder){
                builder=(MessageChainBuilder) obj;
            }
        }
        boolean autoSend=info.autoSend().length==0? BotFrameworkConfig.autoSend:info.autoSend()[0];
        boolean quoteReply=info.quoteReply().length==0?BotFrameworkConfig.quoteReply:info.quoteReply()[0];
        boolean isNative=info.isNative().length==0?BotFrameworkConfig.eventNative:info.isNative()[0];
        boolean atBot=info.atBot().length==0?BotFrameworkConfig.eventAtBot:info.atBot()[0];
        boolean transTraditional=info.transTraditional().length==0?BotFrameworkConfig.transTraditional:info.transTraditional()[0];
        boolean isBlackListType=info.blackListType().length==0?BotFrameworkConfig.blackListType:info.blackListType()[0];
        boolean helperSendImage=info.helpSendImage().length==0?BotFrameworkConfig.helperSendImage:info.helpSendImage()[0];
        boolean atSender=info.atSender().length==0?BotFrameworkConfig.atSender:info.atSender()[0];
        //??????native
        if(isNative){
            if(messageInfo!=null){
                MessageChain message = event.getMessage();
                String messageContext=ValUtil.getMessageContext(message);
                messageInfo.setVal(ValUtil.getMessageInfo(message,messageContext));
            }
            return joinPoint.proceed();
        }
        MessageChain message = event.getMessage();
        String messageContext=ValUtil.getMessageContext(message);
        //????????????
        if(transTraditional) messageContext= Converter.SIMPLIFIED.convert(messageContext);

        //?????????????????????
        //???????????????@??????????????????
        if(atBot&&!VerifyEventUtil.verifyAtBot(message)) return null;
        //????????????
        loop:
        if(info.satisfyAll()){
            //?????????????????????
            //???????????????????????????????????????????????????????????????????????????
            //??????????????????????????????????????????????????????
            if(info.equals().length!=0){
                boolean success=false;
                for (String require : info.equals()) {
                    if(require.equals(messageContext)){
                        success=true;
                        break;
                    }
                }
                if(!success) return null;
            }
            if(info.regex().length!=0){
                boolean success=false;
                for (String require : info.regex()) {
                    Matcher matcher = Pattern.compile(require).matcher(messageContext);
                    if(matcher.find()){
                        success=true;
                        break;
                    }
                }
                if(!success) return null;
            }
            if(info.startWith().length!=0){
                boolean success=false;
                for (String require : info.startWith()) {
                    if(messageContext.startsWith(require)){
                        success=true;
                        break;
                    }
                }
                if(!success) return null;
            }
            if(info.endsWith().length!=0){
                boolean success=false;
                for (String require : info.endsWith()) {
                    if(messageContext.endsWith(require)){
                        success=true;
                        break;
                    }
                }
                if(!success) return null;
            }
            if(info.contains().length!=0){
                boolean success=false;
                for (String require : info.contains()) {
                    if(messageContext.contains(require)){
                        success=true;
                        break;
                    }
                }
                if(!success) return null;
            }
        }else {
            boolean flag=false;
            if(info.contains().length!=0){
                for (String require : info.contains()) {
                    if(messageContext.contains(require)) break loop;
                }
                flag=true;
            }
            if(info.equals().length!=0){
                for (String require : info.equals()) {
                    if(require.equals(messageContext)) break loop;
                }
                flag=true;
            }

            if(info.startWith().length!=0){
                for (String require : info.startWith()) {
                    if(messageContext.startsWith(require)) break loop;
                }
                flag=true;
            }
            if(info.endsWith().length!=0){
                for (String require : info.endsWith()) {
                    if(messageContext.endsWith(require)) break loop;
                }
                flag=true;
            }
            if(info.regex().length!=0){
                for (String require : info.equals()) {
                    Matcher matcher = Pattern.compile(require).matcher(messageContext);
                    if(matcher.find()) break loop;
                }
                flag=true;
            }
            if(flag) return null;
        }
        //???????????????
        if(append==null && event instanceof GroupMessageEvent){
            //?????????????????????/??????????????????
            Matcher matcher = skipMsgPattern.matcher(messageContext);
            if(matcher.find()) return null;

            //??????????????????
            Long groupId = event.getSubject().getId();
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            String name = ValUtil.getName(method);
            if(BotFrameworkConfig.globalBanned.contains(name)){
                event.getSubject().sendMessage("?????????????????????????????????????????????\uD83D\uDC95~");
                return null;
            }
            if(isBlackListType){
                if (BotFrameworkConfig.blackList.get(name).contains(groupId)){
                    event.getSubject().sendMessage("??????????????????????????????~");
                    return null;
                }
            }else {
                if(!BotFrameworkConfig.whiteList.get(name).contains(groupId)){
                    event.getSubject().sendMessage("??????????????????????????????~");
                    return null;
                }
            }
        }

        //??????BotMessageInfo
        if(messageInfo!=null) messageInfo.setVal(ValUtil.getMessageInfo(message,messageContext));

        //????????????
        if(builder==null) builder=new MessageChainBuilder();
        if(quoteReply) builder.append(new QuoteReply(event.getSource()));
        if(info.needWait()) event.getSubject().sendMessage(info.waitMessage());
        if(atSender) builder.append(new At(event.getSender().getId())).append(new PlainText(" "));
        Object proceed = joinPoint.proceed();
        if(!info.appendToken().equals("") && !proceed.equals(false) && !proceed.equals(ListeningStatus.STOPPED)){
            addAppendEvent(info.appendToken(),proceed,(AbstractEvent) event);
        }
        if(!autoSend) return proceed;
        if(proceed!=null){
            if(!info.appendToken().equals("")){

            }else if(proceed instanceof Message){
                builder.append((Message) proceed);
            }else if(proceed instanceof String){
                builder.append((String)proceed);
            }else if(proceed instanceof MessageChainBuilder){
                MessageChainBuilder result=(MessageChainBuilder) proceed;
                if(!builder.equals(result)){
                    builder.append(result.build());
                }
            }else if(proceed instanceof Iterable){
                Iterator iterator = ((Iterable) proceed).iterator();
                Object obj=null;
                while (iterator.hasNext()){
                    obj=iterator.next();
                    if(obj instanceof Message){
                        builder.append((Message) obj);
                    }else if(obj instanceof String){
                        builder.append((String)obj);
                    }else {
                        throw new MoriBotException("???????????????????????????");
                    }
                }
            }else {
                if(append==null && info.appendToken().equals("")) throw new MoriBotException("???????????????????????????");
            }
        }
        if(builder.isEmpty()) return null;
        if(info.forwardMessage()){
            ForwardMessageBuilder forwardBuilder=new ForwardMessageBuilder(event.getSender());
            forwardBuilder.setDisplayStrategy(BotFrameworkConfig.forwardDisplay);
            for (SingleMessage singleMessage : builder) {
                forwardBuilder.add(event.getSender(),singleMessage);
            }
            builder.clear();
            builder.append(forwardBuilder.build());
        }
        MessageReceipt<Contact> receipt = event.getSubject().sendMessage(builder.build());
        if(info.recallTime()!=-1) receipt.recallIn(info.recallTime()*1000);
        return proceed;
    }

    /**
     * ????????????????????????
     */
    private void addAppendEvent(String token,Object proceed,AbstractEvent parentEvent){
        Method appendMethod = BotFrameworkConfig.appendEvent.get(token);
        if(appendMethod==null) throw new MoriBotException("???????????????????????????token?????????????????????");
        int location=0,eventType=-1;
        Class<? extends AbstractEvent> eType=null;
        for (Class<?> parameterType : appendMethod.getParameterTypes()) {
            if(AbstractEvent.class.isAssignableFrom(parameterType)) {
                eventType=location;
                eType=parameterType.asSubclass(AbstractEvent.class);
                break;
            }
            location++;
        }
        if(eventType==-1) throw new MoriBotException("????????????????????????????????????????????????????????????????????????????????????????????????");


        ListenEventAppend append = appendMethod.getAnnotation(ListenEventAppend.class);
        String uuid= UUID.randomUUID().toString();
        int finalEventType = eventType;
        Listener<? extends AbstractEvent> listener = BotFrameworkConfig.bot.getEventChannel().subscribeAlways(eType, event -> {
            if(!(parentEvent instanceof MessageEvent)) return;
            if(event.equals(parentEvent)) return;
            if(!(event instanceof MessageEvent && append.replyFrontSender() && ((MessageEvent)event).getSender().getId()==((MessageEvent)parentEvent).getSender().getId())) return;
            Class<?> eventClass = appendMethod.getDeclaringClass();
            Object eventBean = context.getBean(eventClass);
            BotExceptionHandler handler=null;
            if(eventBean instanceof BotExceptionHandler) handler=(BotExceptionHandler) eventBean;
            Object[] obj=new Object[appendMethod.getParameterCount()];
            int i=0;
            for (Class<?> parameterType : appendMethod.getParameterTypes()) {
                if(AbstractEvent.class.isAssignableFrom(parameterType)){

                }else if(MessageChainBuilder.class.isAssignableFrom(parameterType)){
                    obj[i]=new MessageChainBuilder();
                }else if(BotMessageInfo.class.isAssignableFrom(parameterType)){
                    obj[i]=new BotMessageInfo();
                }else if(List.class.isAssignableFrom(parameterType)){
                    obj[i]=new ArrayList<>();
                }else if(Map.class.isAssignableFrom(parameterType)){
                    obj[i]=new HashMap<>();
                }else if(parameterType.isAssignableFrom(Object.class)){
                    obj[i]=proceed;
                }else {
                    try {
                        obj[i]=parameterType.getDeclaredConstructor().newInstance();
                    } catch (Throwable e) {
                        log.error(eventBean.getClass().getName()+"---"+appendMethod.getName(),e);
                        if(handler!=null){
                            if(e instanceof NullPointerException) e=new MoriBotException("??????????????????????????????");
                            handler.handleException(event,e);
                        }
                    }
                }
                i++;
            }
            obj[finalEventType]=event;
            try {
                Object result = appendMethod.invoke(eventBean, obj);
                //?????????????????????????????????????????????????????????
                if(result==null) return;
                if(result.equals(false)||result.equals(ListeningStatus.STOPPED)) {
                    BotFrameworkConfig.appendEventListener.get(uuid).complete();
                    return;
                }else if(result.equals(true)||result.equals(ListeningStatus.LISTENING)){
                    return;
                }
                //??????????????????????????????????????????????????????????????????????????????????????????????????????true??????listening
                if(append.appendToken().equals("")){
                    BotFrameworkConfig.appendEventListener.get(uuid).complete();
                    return;
                }
                //??????????????????????????????messageChainBuilder????????????????????????????????????????????????????????????
                //????????????????????????????????????????????????true??????ListeningStatus.LISTENING?????????????????????MessageChainBuilder?????????????????????autoSend?????????true
                //???????????????????????????null?????????????????????????????????????????????????????????????????????
                if(result instanceof Message || result instanceof String || result instanceof MessageChainBuilder){
                    BotFrameworkConfig.appendEventListener.get(uuid).complete();
                }else if(result instanceof Iterator){
                    boolean hasNext = ((Iterator) result).hasNext();
                    if(hasNext){
                        Object o = ((Iterator) result).next();
                        if(o instanceof Message || o instanceof String || o instanceof MessageChainBuilder){
                            BotFrameworkConfig.appendEventListener.get(uuid).complete();
                        }
                    }
                }
            } catch (Throwable e) {
                log.error(eventBean.getClass().getName()+"---"+appendMethod.getName(),e);
                if(handler!=null){
                    if(e instanceof NullPointerException) e=new MoriBotException("??????????????????????????????");
                    handler.handleException(event,e);
                }
            }
        });
        BotFrameworkConfig.appendEventListener.put(uuid,listener);
        //???????????????????????????????????????????????????(????????????????????????) ??????????????????listener??????
        String taskUUID = UUID.randomUUID().toString();
        int maxListenTime = append.maxListenTimeMinute().length == 0 ? BotFrameworkConfig.appendListenerListenTimeMinute : append.maxListenTimeMinute()[0];
        scheduled.schedule(new CancelEventListenTask(uuid,taskUUID),new Date(System.currentTimeMillis()+maxListenTime*1000*60));
    }

    public static class CancelEventListenTask implements Runnable{

        private String eventUUID;

        private String threadUUID;

        public CancelEventListenTask(String eventUUID, String threadUUID) {
            this.eventUUID = eventUUID;
            this.threadUUID = threadUUID;
        }

        @Override
        public void run() {
            Listener<? extends AbstractEvent> listener = BotFrameworkConfig.appendEventListener.getOrDefault(eventUUID, null);
            if(listener!=null&&!listener.isCompleted()) listener.complete();
            BotFrameworkConfig.appendEventListener.remove(eventUUID);
            BotFrameworkConfig.scheduledTaskMap.remove(threadUUID);
        }
    }
}
