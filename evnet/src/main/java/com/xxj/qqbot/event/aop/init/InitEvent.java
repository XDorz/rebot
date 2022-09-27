package com.xxj.qqbot.event.aop.init;

import com.xxj.qqbot.util.botconfig.functioncompent.ListenEvent;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;

import com.xxj.qqbot.util.botconfig.functioncompent.ListenEventAppend;
import com.xxj.qqbot.util.botconfig.init.BotFrameworkConfigLoad;
import com.xxj.qqbot.util.botconfig.init.ImageInit;
import com.xxj.qqbot.util.common.BotMessageInfo;
import com.xxj.qqbot.util.common.ConfigLoaderUtil;
import com.xxj.qqbot.util.common.MoriBotException;
import com.xxj.qqbot.util.common.ValUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InitEvent {

    /**
     * 初始化时间置于图片缓存加载之后
     */
    @Autowired
    ImageInit imageInit;

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    private void init(){
        if(!BotFrameworkConfig.enableListenEvent) return;
        log.info("将所有监听事件注册入监听事件链");
        List<Class<?>> classes = ConfigLoaderUtil.fetchAnnoClass(BotEvents.class);
        for (Class<?> clazz : classes) {
            //从ioc容器中取出bean
            Object obj=applicationContext.getBean(clazz);
            loop:
            for (Method method : clazz.getMethods()) {
                ListenEventAppend append=null;
                if((append=method.getAnnotation(ListenEventAppend.class))!=null){
                    if(method.getReturnType().getName().equals("void")||Void.class.isAssignableFrom(method.getReturnType())){
                        log.error("\n"+clazz.getName()+"---"+method.getName()+"---拓展监听方法返回值不能设置为void，同时，返回值也不能为null，" +
                                "\n否则将导致拓展监听方法无法在最大监听时间到达前无法被取消，建议您将返回值更改外Object\n" +
                                "您可以通过返回true/false或者ListeningStatus.LISTENING/STOPPED 来自由选择是否继续监听拓展事件\n" +
                                "此方法未加入监听拓展监听事件表");
                    }else {
                        BotFrameworkConfig.appendEvent.put(append.token(),method);
                    }
                    continue ;
                }
                //获取带有注解标注的方法
                ListenEvent eventMethod=null;
                if((eventMethod=method.getAnnotation(ListenEvent.class))==null) continue;

                //如果功能注明native的话将不为其附加帮助功能与黑白名单功能
                //因其可能为监听戳一戳，加好友等无需注明的功能
                if(!(eventMethod.isNative().length!=0&& eventMethod.isNative()[0])){
                    String name = ValUtil.getName(method);
                    String functionType=eventMethod.type();
                    String help = eventMethod.help();
                    if(functionType.equals("")) functionType="基础功能";
                    if(!BotFrameworkConfig.blackList.containsKey(name)){
                        BotFrameworkConfig.blackList.put(name,new HashSet<>());
                    }
                    if(!BotFrameworkConfig.whiteList.containsKey(name)){
                        BotFrameworkConfig.whiteList.put(name,new HashSet<>());
                    }
                    if(!help.equals("")) BotFrameworkConfig.functionHelp.put(name,help);
                    if(BotFrameworkConfig.menu.containsKey(functionType)){
                        BotFrameworkConfig.menu.get(functionType).add(name);
                    }else {
                        List<String> list=new ArrayList<>();
                        list.add(name);
                        BotFrameworkConfig.menu.put(functionType,list);
                    }
                }

                //确定参数中是否带有监听的event类型
                Class<? extends AbstractEvent> eventType=null;
                int parameterCount = method.getParameterCount();
                int eventLocation=-1;
                //event事件记录其参数位置与类型，map和list类型直接实例化子实现类
                for (Class<?> type : method.getParameterTypes()) {
                    eventLocation++;
                    if (AbstractEvent.class.isAssignableFrom(type)){
                        eventType=type.asSubclass(AbstractEvent.class);
                        break ;
                    }
                }
                if(eventType==null){
                    log.error(clazz.getName()+"类中"+method.getName()+"方法未指明监听事件类型！");
                    continue;
                }
                //监听类对象
                Object finalObj = obj;
                int finalLocation = eventLocation;
                //注册监听事件
                BotFrameworkConfig.bot.getEventChannel().subscribeAlways(eventType, event->{
                    //初始化参数，实例化所有参数对象
                    Object[] params=new Object[parameterCount];
                    int location=-1;
                    for (Class<?> type : method.getParameterTypes()) {
                        location++;
                        if (AbstractEvent.class.isAssignableFrom(type)){
                            continue;
                        }else if(MessageChainBuilder.class.isAssignableFrom(type)){
                            params[location]=new MessageChainBuilder();
                        }else if(BotMessageInfo.class.isAssignableFrom(type)){
                            params[location]=new BotMessageInfo();
                        }else if(Map.class.isAssignableFrom(type)){
                            params[location]=new HashMap<String,Object>();
                        }else if(List.class.isAssignableFrom(type)){
                            params[location]=new ArrayList<Object>();
                        }else {
                            try {
                                params[location]=type.getConstructor().newInstance();
                            } catch (InstantiationException e) {
                                log.error(type.getName()+"类实例化失败，"+method.getName()+"方法未注册入监听事件",e);
                                return;
                            } catch (IllegalAccessException e) {
                                log.error(type.getName()+"类中构造方法无法操作，"+method.getName()+"方法未注册入监听事件",e);
                                return;
                            } catch (InvocationTargetException e) {
                                log.error(type.getName()+"类中方法返回值类型错误，"+method.getName()+"方法未注册入监听事件",e);
                                return;
                            } catch (NoSuchMethodException e) {
                                log.error(type.getName()+"类中没有无参构造函数"+method.getName()+"方法未注册入监听事件",e);
                                return;
                            }
                        }
                    }
                    //注入event事件信息
                    params[finalLocation]=event;
                    try {
                        method.invoke(finalObj,params);
                    } catch (IllegalAccessException e) {
                        log.error(clazz.getName()+"类中"+method.getName()+"方法无法操作",e);
                    } catch (InvocationTargetException e) {
                        log.error(clazz.getName()+"类中"+method.getName()+"方法调用失败",e.getTargetException());
                        if(BotExceptionHandler.class.isAssignableFrom(clazz)){
                            Throwable throwable=e.getTargetException();
                            if (throwable instanceof NullPointerException){
                                throwable=new MoriBotException("空指针异常",throwable);
                            }
                            ((BotExceptionHandler)finalObj).handleException(event,throwable);
                        }
                    }catch (Throwable e){
                        if(BotExceptionHandler.class.isAssignableFrom(clazz)){
                            ((BotExceptionHandler)finalObj).handleException(event,e);
                        }else {
                            log.error(clazz.getName()+"---"+method.getName()+"方法出现错误："+e.getMessage(),e);
                        }
                    }
                });
            }
        }
        BotFrameworkConfigLoad.WriteBanedJson();
    }
}
