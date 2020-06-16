package com.vladmykol.takeandcharge.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
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
    public static final String clientTaskExecutorName = "clientTaskExecutor";
    public static final String serverTaskExecutorName = "serverTaskExecutor";
    public static final String returnRentTaskExecutorName = "returnRentTaskExecutor";

    @Bean(name = serverTaskExecutorName)
    public Executor serverTaskExecutor() {
        log.debug("Creating Async Task Executor for Server");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("server-");
        executor.initialize();
        return executor;
    }

    @Bean(name = clientTaskExecutorName)
    public Executor clientTaskExecutor() {
        log.debug("Creating Async Task Executor for Clients");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("client-");
        executor.initialize();
        return executor;
    }

    @Bean(name = returnRentTaskExecutorName)
    public Executor returnRentTaskExecutor() {
        log.debug("Creating Async Task Executor for returning rent requests");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("rent-return-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setDaemon(true);

        return scheduler;
    }

}
