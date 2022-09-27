package com.xxj.qqbot.util.botconfig.init;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Configuration
public class ScheduleTaskConfig {

    public static ConcurrentHashMap<String, ScheduledFuture> futureMap = new ConcurrentHashMap<>();

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(20);                        // 线程池大小
        threadPoolTaskScheduler.setThreadNamePrefix("FrameWorkTaskExecutor-");   // 线程名称
        threadPoolTaskScheduler.setAwaitTerminationSeconds(15);         // 线程执行最大等待时长
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);  // 调度器shutdown被调用时等待当前被调度的任务完成
        return threadPoolTaskScheduler;
    }
}
