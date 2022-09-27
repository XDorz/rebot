package com.xxj.qqbot.util.botconfig.functioncompent.configload;

import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 获取启动类上的注解的basePackages
 * basePackages用于让ConfigLoaderUtil扫描被注解标记的类
 * @see com.xxj.qqbot.util.common.ConfigLoaderUtil
 */
//todo 宜用更适合的方式获取启动类上的注解信息
@Slf4j
public class BotConfigScanPath implements ImportBeanDefinitionRegistrar{

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableBotConfigScan.class.getCanonicalName());
        Set<String> basePackages = new HashSet<>();
        if(attributes==null){
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }else {
            // basePackages 属性是否有配置值，如果有则添加
            for (String pkg : (String[]) attributes.get("basePackages")) {
                if (StringUtils.hasText(pkg)) {
                    basePackages.add(pkg);
                }
            }
            // 如果上面没有获取到basePackages，那么这里就默认使用当前项目启动类所在的包为basePackages
            if (basePackages.isEmpty()) {
                basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
            }
        }
        //设置类扫描范围
        setScanPath(basePackages);
    }

    /**
     * 设置类扫描范围
     */
    private static void setScanPath(Set<String> basePackages){
        if(BotFrameworkConfig.scanPath!=null){
            basePackages.addAll(Arrays.asList(BotFrameworkConfig.scanPath));
        }
        BotFrameworkConfig.scanPath=basePackages.toArray(String[]::new);
    }
}
