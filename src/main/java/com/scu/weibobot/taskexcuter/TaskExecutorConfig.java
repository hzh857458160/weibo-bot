package com.scu.weibobot.taskexcuter;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@ComponentScan("com.scu.weibobot.taskexcuter")
@EnableAsync //利用@EnableAsync注解开启异步任务支持
public class TaskExecutorConfig implements AsyncConfigurer {
    //配置类实现AsyncConfigurer接口并重写getAsyncExcutor方法，并返回一个ThreadPoolTaskExevutor
    //这样我们就获得了一个基于线程池的TaskExecutor
     /*
    此处成员变量应该使用@Value从配置中读取
     */
    private int corePoolSize = 10;
    private int maxPoolSize = 100;
    private int queueCapacity = 10;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);//线程池维护线程的最少数量
        taskExecutor.setMaxPoolSize(maxPoolSize);//线程池维护线程的最大数量
        taskExecutor.setQueueCapacity(queueCapacity);//线程池所使用的缓冲队列
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
