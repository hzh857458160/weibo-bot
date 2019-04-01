package com.scu.weibobot.utils;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.service.RedisService;
import com.scu.weibobot.service.WeiboAccountService;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class SetInfoUtilTest {

    @Autowired
    private BotInfoService botInfoService;

    @Autowired
    private WeiboAccountService accountService;

    @Autowired
    private RedisService redisService;


    @Test
    public void setBotInfo() {


        WebDriver driver = null;
        try {
//            driver = WebDriverPool.getWebDriver();
//
//            String username = "13887874056";
//            String password = "WEIBOhzh.0123";
//            WeiboOpUtil.loginWeibo(driver, username, password);
//            WeiboOpUtil.commentWeibo(driver, WeiboOpUtil.getWeiboList(driver).get(1), "好");


            log.info("a");
            log.info("b");
        } catch (Exception e){
            e.printStackTrace();

        } finally {
            WebDriverPool.closeWebDriver(driver);
        }


    }

    private void doSomethingInWeibo(String interest, String username, String password) {
        WebDriver driver = null;
        try {
            driver = WebDriverPool.getWebDriver();
            WeiboOpUtil.loginWeibo(driver, username, password);
            Random random = new Random();
            int postWeiboRandom = 60;
            if (postWeiboRandom <= 40) {
//                int interestRandom = random.nextInt(3);
                String content = GenerateInfoUtil.generatePostContent(interest);
                WeiboOpUtil.postWeibo(driver, content, false);
            }
            Thread.sleep(2000);
            List<WebElement> weiboList = WeiboOpUtil.getWeiboList(driver);
            for (int i = 0; i < weiboList.size(); i++) {
                WebElement weibo = weiboList.get(i);
                WeiboOpUtil.scrollToElement(driver, weibo);
                //30%的概率遇到感兴趣的微博，仔细阅读并点赞
                int likeRandom = random.nextInt(100) + 1;
                if (likeRandom <= 50) {
                    Thread.sleep((long) ((random.nextDouble() + 2.0) * 1000));
                    WeiboOpUtil.likeWeibo(weibo);
                } else {
                    //普通微博，简单阅读几秒
                    Thread.sleep((long) ((random.nextDouble() + 0.7) * 1000));
                }
                //20%的概率转发
                int reportRandom = random.nextInt(100) + 1;
                if (reportRandom <= 50) {
                    WeiboOpUtil.reportWeibo(driver, weibo, "");
                    weiboList = WeiboOpUtil.getWeiboList(driver);
                }
                //20%的概率评论
                int commentRandom = random.nextInt(100) + 1;
                if (commentRandom <= 50) {
//                    WeiboOpUtil.commentWeibo(driver, weibo, "");
//                    weiboList = WeiboOpUtil.getWeiboList(driver);
                }
//                WebDriverUtil.scrollWeibo(driver, 300);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            WebDriverPool.closeWebDriver(driver);
        }

    }
}