package com.scu.weibobot.taskexcuter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@ComponentScan("com.scu.weibobot.taskexcuter")
@EnableAsync //利用@EnableAsync注解开启异步任务支持
@PropertySource("classpath:config.properties")
@Slf4j
public class TaskExecutorConfig implements AsyncConfigurer {
    //配置类实现AsyncConfigurer接口并重写getAsyncExcutor方法，并返回一个ThreadPoolTaskExevutor
    //这样我们就获得了一个基于线程池的TaskExecutor
    @Value("$(CORE_POOL_SIZE)")
    private String corePoolSize;
    @Value("$(MAX_POOL_SIZE)")
    private String maxPoolSize;
    @Value("$(QUEUE_CAPACITY)")
    private String queueCapacity;

    @Override
    public Executor getAsyncExecutor() {
        log.info("线程池设置：{},{},{}", corePoolSize, maxPoolSize, queueCapacity);
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(Integer.parseInt(corePoolSize));//线程池维护线程的最少数量
        taskExecutor.setMaxPoolSize(Integer.parseInt(maxPoolSize));//线程池维护线程的最大数量
        taskExecutor.setQueueCapacity(Integer.parseInt(queueCapacity));//线程池所使用的缓冲队列
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
