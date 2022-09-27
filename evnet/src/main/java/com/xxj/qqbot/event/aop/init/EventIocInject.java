package com.xxj.qqbot.event.aop.init;

import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 扫描带 @see BotEvents 注解的类加入ioc容器
 */
@Slf4j
public class EventIocInject implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    /**
     * 资源加载器
     */
    private ResourceLoader resourceLoader;
    /**
     * 环境
     */
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        log.info("扫描所有监听事件类并装入ioc容器");
        // 获取指定要扫描的basePackages
        Set<String> basePackages = getBasePackages(metadata);
        //对象无值，表示启动类未添加注解，不开启事件自动添加和扫描
        if(basePackages==null){
            BotFrameworkConfig.enableListenEvent=false;
            return;
        }
        BotFrameworkConfig.enableListenEvent=true;
        //设置类扫描范围
        setScanPath(basePackages);

        // 创建scanner
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(resourceLoader);

        // 设置扫描器scanner扫描的过滤条件
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(BotEvents.class);
        scanner.addIncludeFilter(annotationTypeFilter);

        // 遍历每一个basePackages
        for (String basePackage : basePackages) {
            // 通过scanner获取basePackage下的候选类(有标@BotEvents注解的类)
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            // 遍历每一个候选类，如果符合条件就把他们注册到容器
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    if(!annotationMetadata.isConcrete()||!annotationMetadata.isIndependent()){
                        log.error(annotationMetadata.getClassName()+"----@BotEvents注解只可标注于类之上！");
                        continue;
                    }
                    // 获取@BotEvents注解的属性
                    Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(BotEvents.class.getCanonicalName());
                    // 注册到容器
                    registerBean(registry, annotationMetadata, attributes);
                    BotFrameworkConfig.boostEvent=true;
                }
            }
        }
    }

    /**
     * 利用factoryBean创建代理对象，并注册到容器
     */
    private static void registerBean(BeanDefinitionRegistry registry,
                                     AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        // 类名（接口全限定名）
        String className = annotationMetadata.getClassName();
        // 创建BeanDefinition
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(className);
        // 解析出bean的别名，如果没有则使用类名，再没有则不使用别名
        String name = getName(attributes);
        if (!StringUtils.hasText(name)) {
            if(className.contains(".")){
                name = className.substring(className.lastIndexOf(".")+1);
            }
        }

        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();

        // 注册bean定义信息到容器
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, new String[]{name});
        // 使用BeanDefinitionReaderUtils工具类将BeanDefinition注册到容器
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    /**
     * 创建扫描器
     */
    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    /**
     * 获取base packages
     */
    protected static Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        // 获取到@EnableSimpleRpcClients注解所有属性
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableBoostEvent.class.getCanonicalName());
        if(attributes==null) return null;
        BotFrameworkConfig.boostEvent=true;
        Set<String> basePackages = new HashSet<>();
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

        //为bot设置账号密码(如果有的话)
        injectBotFrameworkConfig(attributes);

        return basePackages;
    }

    /**
     * 获取name
     */
    protected static String getName(Map<String, Object> attributes) {
        String name = (String) attributes.get("name");
        if (!StringUtils.hasText(name)) {
            name = (String) attributes.get("value");
        }
        if(!StringUtils.hasText(name)){
            name=null;
        }
        return name;
    }

    /**
     * 如果启动类的 @EnableBoostEvent带有account与password，则将其放入全局配置
     */
    private static void injectBotFrameworkConfig(Map<String, Object> attributes){
        String account=(String)attributes.get("account");
        if(!StringUtils.hasText(account)) return;
        //检查account是否可以转换为long
        Matcher matcher = Pattern.compile("\\d+").matcher(account);
        if(!matcher.matches()) return;
        BotFrameworkConfig.botId=Long.parseLong(account) ;
        BotFrameworkConfig.password=(String) attributes.get("password");
        String rootId = attributes.get("rootId").toString();
        matcher = Pattern.compile("\\d+").matcher(rootId);
        if(!matcher.matches()){
            log.error("请在启动项@EnableBoostEvent中注明最高管理员的id！！！");
            System.exit(-1);
        }
        BotFrameworkConfig.rootId=Long.parseLong(rootId);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
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
