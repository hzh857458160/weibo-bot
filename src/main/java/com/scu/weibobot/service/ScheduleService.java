package com.scu.weibobot.service;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.consts.CronConst;
import com.scu.weibobot.taskexcuter.WeiboBotExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class ScheduleService {
    private List<WeiboAccount> weiboAccountList = null;
    private List<BotInfo> botInfoList;

    @Autowired
    private WeiboAccountService accountService;

    @Autowired
    private BotInfoService botInfoService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Scheduled(cron= CronConst.RUN_PER_HOUR_CRON)
    public void coreJob(){
        log.info("{}", LocalTime.now());
//        log.info("当前线程数目为{}", Thread.activeCount());
        //获取所有可用的微博机器人账号（需要缓存）
        weiboAccountList = accountService.findAllAccount();
        botInfoList = new ArrayList<>();
        //根据账号去寻找对应的资料
        for (WeiboAccount account : weiboAccountList){
            BotInfo temp = botInfoService.findBotInfoByAccountId(account.getAccountId());
            botInfoList.add(temp);

            //然后设置好WeiboBotExecuter的属性
            WeiboBotExecutor botExecutor = new WeiboBotExecutor();
            botExecutor.setBotInfo(temp);
            botExecutor.setWeiboAccount(account);

            //使用线程池调用WeiboBotExecuter的run方法
            taskExecutor.execute(botExecutor);
        }

    }

}
