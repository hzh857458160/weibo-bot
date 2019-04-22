package com.scu.weibobot.service;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.taskexcuter.WeiboBotExecutor;
import com.scu.weibobot.utils.GenerateInfoUtil;
import com.scu.weibobot.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

/**
 * ClassName: ScheduleService
 * ClassDesc: 定时器服务类
 * Author: HanrAx
 * Date: 2019/02/10
 **/
@Service
@Slf4j
public class ScheduleService {

    @Autowired
    private WeiboAccountService accountService;
    @Autowired
    private BotInfoService botInfoService;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 核心定时任务
     * 自动使用线程运行社交机器人
     * 频率为一个小时一次
     */
//    @Scheduled(cron= Consts.RUN_PER_HOUR_CRON)
//    public void coreJob(){
//        log.info("进入定时任务coreJob() {}", LocalTime.now());
//        //获取所有可用的微博机器人账号
//        List<WeiboAccount> weiboAccountList = accountService.findAllAccount();
//        //根据账号去寻找对应的资料
//        for (WeiboAccount account : weiboAccountList){
//            BotInfo botInfo = botInfoService.findBotInfoByAccountId(account.getAccountId());
//            if (readyToStart(botInfo)) {
//                //然后设置好WeiboBotExecuter的属性
//                WeiboBotExecutor botExecutor = new WeiboBotExecutor();
//                botExecutor.setBotInfo(botInfo);
//                botExecutor.setWeiboAccount(account);
//                //使用线程池调用WeiboBotExecuter的run方法
//                taskExecutor.execute(botExecutor);
//                waitSeconds(2);
//            } else {
//                log.info("bot{}尚未达到概率", botInfo.getBotId());
//            }
//
//        }
//
//    }

    /**
     * 判断当前机器人是否达到启动概率
     *
     * @param botInfo 机器人信息
     * @return
     */
    private boolean readyToStart(BotInfo botInfo) {
        //获取使用度对应的概率
        double prob = GenerateInfoUtil.getUseWeiboProb(botInfo.getBotLevel());
        log.info("对应使用度概率为{}", prob);
        //随机数
        int nowProb = new Random().nextInt(100) + 1;
        log.info("当前随机数为{}", nowProb);
        return (nowProb / 100) < prob;
    }

}
