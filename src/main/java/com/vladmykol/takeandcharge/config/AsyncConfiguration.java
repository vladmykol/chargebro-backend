package com.vladmykol.takeandcharge.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class AsyncConfiguration {
    public static final String RENT_REFRESH_TASK_EXECUTOR = "rentRefreshTaskExecutor";
    public static final String STATION_LISTENER_TASK_EXECUTOR = "stationListenerTaskExecutor";
    public static final String STATION_SERVER_TASK_EXECUTOR = "stationServerTaskExecutor";

    @Bean(name = STATION_SERVER_TASK_EXECUTOR)
    public Executor serverTaskExecutor() {
        log.debug("Creating Async Task Executor for Station server");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(2);
        executor.setThreadNamePrefix("server-");
        executor.initialize();
        return executor;
    }

    @Bean(name = STATION_LISTENER_TASK_EXECUTOR)
    public Executor clientTaskExecutor() {
        log.debug("Creating Async Task Executor for Active stations");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("station-");
        executor.initialize();
        return executor;
    }

    @Bean(name = RENT_REFRESH_TASK_EXECUTOR)
    public Executor refreshTaskExecutor() {
        log.debug("Creating Async Task Executor for Rent refresh action");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("rentRefresh-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setDaemon(true);
        return scheduler;
    }

}
