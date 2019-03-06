package com.scu.weibobot.taskexcuter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;



@Component
public class AsyncTaskService {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    //通过@Async注解表明该方法是个异步方法，如果注解在类级别，则表明该类所有的方法都是异步方法。
    // 而这里的方法自动被注入使用ThreadPoolTaskExecutor作为TaskExecutor
    @Async
    public void executeAsyncTask(String searchKey){

    }


    public void schedule(Runnable task, String cron){
        if(cron == null || "".equals(cron)) {
            cron = "0 * * * * *";
        }
        threadPoolTaskScheduler.schedule(task, new CronTrigger(cron));
    }


}
