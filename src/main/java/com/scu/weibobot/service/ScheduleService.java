package com.scu.weibobot.service;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.ProxyIp;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.Consts;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import com.scu.weibobot.taskexcuter.WeiboBotExecutor;
import com.scu.weibobot.utils.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
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

    @Autowired
    private WeiboAccountService accountService;
    @Autowired
    private BotInfoService botInfoService;
    @Autowired
    private ProxyIpService proxyIpService;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Scheduled(cron= Consts.RUN_PER_HOUR_CRON)
    public void coreJob(){
        log.info("{}", LocalTime.now());
//        log.info("当前线程数目为{}", Thread.activeCount());
        //获取所有可用的微博机器人账号（需要缓存）
        List<WeiboAccount> weiboAccountList = accountService.findAllAccount();
        List<BotInfo> botInfoList = new ArrayList<>();
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

    @Scheduled(cron = Consts.RUN_PER_DAY_CRON)
    public void searchForProxy(){
        WebDriver driver = null;
        try {
            driver = WebDriverPool.getWebDriver();
            List<String> locationList = botInfoService.findAllBotLocations();
            ProxyUtil.initProxyLocation(locationList);
            List<ProxyIp> proxyIpList = new ArrayList<>(300);
            proxyIpList.addAll(ProxyUtil.crawlFrom89IP(driver));
            proxyIpList.addAll(ProxyUtil.crawlFromKUAIDAILI(driver));
            proxyIpList.addAll(ProxyUtil.crawlFromQIYUN(driver));
            proxyIpList.addAll(ProxyUtil.crawlFromZDAYE(driver));
            proxyIpService.addAllProxyIp(proxyIpList);

        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            if (driver != null){
                WebDriverPool.closeAndReturnToPool(driver);
            }
        }




    }

}
