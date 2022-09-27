package com.xxj.qqbot.util.botconfig.init;

import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.BotRegister;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvLoad;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvPrefix;
import com.xxj.qqbot.util.common.ConfigLoaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

@Slf4j
@Component
public class EnvironmentInit {

    @Autowired
    Environment environment;

    @PostConstruct
    private void init(){
        log.info("开始注入spring环境变量，非static的属性将被忽略！");
        List<Class<?>> annoClass = ConfigLoaderUtil.fetchAnnoClass(EnvPrefix.class);
        for (Class<?> clazz : annoClass) {
            if(BotRegister.class.isAssignableFrom(clazz)) continue;
            EnvPrefix envPrefix = clazz.getAnnotation(EnvPrefix.class);
            String prefix="";
            if (envPrefix!=null){
                prefix = envPrefix.prefix().equals("")?"":envPrefix.prefix()+".";
            }
            for (Field field : clazz.getDeclaredFields()){
                if(field.getName().toLowerCase().contains("log")) continue;
                if(!Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers())) continue;
                boolean canAccess=field.canAccess(null);
                field.setAccessible(true);
                EnvLoad envLoad = field.getAnnotation(EnvLoad.class);
                String name=prefix;
                try {
                    if(envLoad!=null){
                        name=envLoad.usePrefix()?name+envLoad.name():envLoad.name().equals("")?field.getName():envLoad.name();
                        if(envLoad.required()){
                            field.set(null,environment.getRequiredProperty(name,field.getType()));
                        }else {
                            field.set(null,environment.getProperty(name,field.getType()));
                        }
                    }else {
                        name=name+field.getName();
                        field.set(null,environment.getRequiredProperty(name, field.getType()));
                    }
                }catch (IllegalAccessException e){
                    log.error(clazz.getName()+"---对属性"+field.getName()+"赋值未能成功，已略过对其赋值！",e);
                }finally {
                    field.setAccessible(canAccess);
                }
            }
        }
    }
}
