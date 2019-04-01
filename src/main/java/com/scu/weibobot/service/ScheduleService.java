package com.scu.weibobot.service;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.consts.Consts;
import com.scu.weibobot.taskexcuter.WeiboBotExecutor;
import com.scu.weibobot.utils.GenerateInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;


@Service
@Slf4j
public class ScheduleService {

    @Autowired
    private WeiboAccountService accountService;
    @Autowired
    private BotInfoService botInfoService;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Scheduled(cron= Consts.RUN_PER_HOUR_CRON)
    public void coreJob(){
        log.info("进入定时任务coreJob() {}", LocalTime.now());
        //获取所有可用的微博机器人账号
        List<WeiboAccount> weiboAccountList = accountService.findAllAccount();
        //根据账号去寻找对应的资料
        for (WeiboAccount account : weiboAccountList){
            BotInfo botInfo = botInfoService.findBotInfoByAccountId(account.getAccountId());
            if (getStartFlag(botInfo)) {
                //然后设置好WeiboBotExecuter的属性
                WeiboBotExecutor botExecutor = new WeiboBotExecutor();
                botExecutor.setBotInfo(botInfo);
                botExecutor.setWeiboAccount(account);
                //使用线程池调用WeiboBotExecuter的run方法
                taskExecutor.execute(botExecutor);
                waitSeconds(2);
            } else {
                log.info("bot{}尚未达到概率", botInfo.getBotId());
            }

        }

    }

    private boolean getStartFlag(BotInfo botInfo) {
        //获取使用度对应的概率
        double prob = GenerateInfoUtil.getUseWeiboProb(botInfo.getBotLevel());
        log.info("对应使用度概率为{}", prob);
        //随机数
        int nowProb = new Random().nextInt(100) + 1;
        log.info("当前随机数为{}", nowProb);
        return (nowProb / 100) < prob;
    }

//    @Scheduled(cron = Consts.RUN_PER_DAY_CRON)
//    public void searchForProxy(){
//        log.info("进入定时任务searchForProxy() {}", LocalTime.now());
//        WebDriver driver = null;
//        try {
//            driver = WebDriverPool.getWebDriver();
//            List<String> locationList = botInfoService.findAllBotLocations();
//            log.info("查找到所有机器人的地址 list size = {}", locationList.size());
//            ProxyUtil.initProxyLocation(locationList);
//            List<ProxyIp> proxyIpList = new ArrayList<>(300);
//            proxyIpList.addAll(ProxyUtil.crawlFrom89IP(driver));
//            proxyIpList.addAll(ProxyUtil.crawlFromKUAIDAILI(driver));
//            proxyIpList.addAll(ProxyUtil.crawlFromQIYUN(driver));
//            proxyIpList.addAll(ProxyUtil.crawlFromZDAYE(driver));
//            proxyIpService.addAllProxyIp(proxyIpList);
//
//        } finally {
//            if (driver != null){
//                WebDriverPool.closeWebDriver(driver);
//            }
//        }
//    }
//
//    @Scheduled(cron = Consts.RUN_PER_DAY_CRON)
//    public void cleanUselessProxy(){
//        log.info("进入定时任务cleanUselessProxy() {}", LocalTime.now());
//        proxyIpService.deleteAllUselessProxy();
//    }



}
