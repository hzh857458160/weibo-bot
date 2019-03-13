package com.scu.weibobot.utils;

import com.scu.weibobot.domain.ProxyIp;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.service.WeiboAccountService;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

//@SpringBootTest
//@RunWith(SpringRunner.class)
public class SetInfoUtilTest {

    @Autowired
    private BotInfoService botInfoService;

    @Autowired
    private WeiboAccountService accountService;


    @Test
    public void setBotInfo() {
        WebDriver driver = null;
        try {
            driver = WebDriverPool.getWebDriver();
            List<ProxyIp> ipList = ProxyUtil.crawlFromQIYUN(driver);
            ipList.forEach(System.out::println);

        } catch (Exception e){
            e.printStackTrace();

        } finally {
            if (driver != null){
                WebDriverPool.closeCurrentWebDriver(driver);
            }
        }


//        System.out.println(ProxyUtil.parseLocationFromKUAIDAILI("北京市 移动"));


//        String username = "jwnilywauekgjgs-aae25@yahoo.com";
//        String password = "WAsbjlttuv07";
//
//        WebDriver driver = WebDriverPool.getWebDriver();
//        WeiboOpUtil.loginWeibo(driver, username, password);
//        long accountId = accountService.findByUsername(username).getAccountId();
//        String interests = botInfoService.findBotInfoByAccountId(accountId).getInterests();
//        System.out.println("interests = " + interests);
//        List<String> list = new ArrayList<>(Arrays.asList(interests.split("#")));
//        list.forEach(System.out::println);
//
//        WeiboOpUtil.subscribeWeiboByInterest(driver, list);
//
//        WebDriverPool.closeCurrentWebDriver(driver);





    }
}