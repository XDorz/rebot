package com.xxj.qqbot;

import com.xxj.qqbot.event.aop.init.EnableBoostEvent;
import com.xxj.qqbot.util.botconfig.functioncompent.configload.EnableBotConfigScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties
@EnableBoostEvent(basePackages = "com.xxj.qqbot.event")
@EnableBotConfigScan(basePackages ={"com.xxj.qqbot.util.botconfig"})
public class MiraiBotApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(MiraiBotApplication.class, args);
    }
}
