package com.scu.weibobot.utils;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.service.RedisService;
import com.scu.weibobot.service.WeiboAccountService;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
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
            driver = WebDriverPool.getWebDriver();
            ProxyUtil.setLocalIpToWhiteListInKUAIDAILI();
//            String username = "13887874056";
//            String password = "WEIBOhzh.0123";
//            WeiboOpUtil.loginWeibo(driver, username, password);
//            WebDriverPool.closeCurrentWebDriver(driver);
//
//            driver = WebDriverPool.getWebDriver();
//            WeiboOpUtil.loginWeibo(driver, username, password);




        } catch (Exception e){
            e.printStackTrace();

        } finally {
            WebDriverPool.closeCurrentWebDriver(driver);
        }


    }
}