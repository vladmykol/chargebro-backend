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
    public static final String STATION_LISTENER_TASK_EXECUTOR = "stationListenerTaskExecutor";
    public static final String STATION_SERVER_TASK_EXECUTOR = "stationServerTaskExecutor";
    public static final String RETURN_POWER_BANK_TASK_EXECUTOR = "returnPowerBankTaskExecutor";
//    public static final String PAYMENT_CALLBACK_TASK_EXECUTOR = "paymentCallbackTaskExecutor";

    @Bean(name = STATION_SERVER_TASK_EXECUTOR)
    public Executor serverTaskExecutor() {
        log.debug("Creating Async Task Executor for Station server");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(30);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("server-");
        executor.initialize();
        return executor;
    }

    @Bean(name = STATION_LISTENER_TASK_EXECUTOR)
    public Executor clientTaskExecutor() {
        log.debug("Creating Async Task Executor for Active stations");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("station-");
        executor.initialize();
        return executor;
    }

    @Bean(name = RETURN_POWER_BANK_TASK_EXECUTOR)
    public Executor rentReturnTaskExecutor() {
        log.debug("Creating Async Task Executor for a returning rent requests from station");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("pb-return-");
        executor.initialize();
        return executor;
    }

//    @Bean(name = PAYMENT_CALLBACK_TASK_EXECUTOR)
//    public Executor rentStartTaskExecutor() {
//        log.debug("Creating Async Task Executor for payment callbacks");
//        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(3);
//        executor.setMaxPoolSize(5);
//        executor.setQueueCapacity(100);
//        executor.setThreadNamePrefix("pay-callback-");
//        executor.initialize();
//        return executor;
//    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setDaemon(true);

        return scheduler;
    }

}
