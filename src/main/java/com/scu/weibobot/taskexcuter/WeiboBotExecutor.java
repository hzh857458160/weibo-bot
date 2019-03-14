package com.scu.weibobot.taskexcuter;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.utils.TimeUtil;
import com.scu.weibobot.utils.WebDriverUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.util.Random;


@Slf4j
public class WeiboBotExecutor implements Runnable {

    private BotInfo botInfo;
    private WeiboAccount weiboAccount;

    public void setBotInfo(BotInfo botInfo) {
        this.botInfo = botInfo;
    }

    public void setWeiboAccount(WeiboAccount weiboAccount) {
        this.weiboAccount = weiboAccount;
    }


    public void run() {

        if (botInfo == null || weiboAccount == null) {
            log.error("WeiboBotExecutor参数有误 [botInfo:{}, weiboAccount:{}]", botInfo, weiboAccount);
            return;
        }
        //获取使用度对应的概率
        double prob = TimeUtil.getProbability(botInfo.getBotLevel());
        log.info("对应使用度概率为{}", prob);
        //随机数
        double nowProb = new Random().nextDouble();
        log.info("当前随机数为{}", nowProb);
        if (nowProb < prob) {
            //进入微博，模拟操作
            log.info("进入微博，模拟操作");
            doSomethingInWeibo();
        }


    }

    private void doSomethingInWeibo() {
        WebDriver driver = WebDriverPool.getWebDriver(botInfo);
        WeiboOpUtil.loginWeibo(driver, weiboAccount.getUsername(), weiboAccount.getPassword());
        Random random = new Random();
        WeiboOpUtil.likeWeibo(driver, random.nextInt(3));
        WebDriverUtil.scrollWeibo(driver, 500);
        WeiboOpUtil.likeWeibo(driver, random.nextInt(3) + 3);
        WebDriverUtil.scrollWeibo(driver, 500);
        WeiboOpUtil.reportWeibo(driver, random.nextInt(3) + 6, "");
//        WeiboOpUtil.postWeibo(driver, "测试，当前时间为" + LocalDateTime.now());
        WebDriverPool.closeCurrentWebDriver(driver);
    }

}
