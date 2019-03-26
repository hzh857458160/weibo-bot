package com.scu.weibobot.controller;

import com.scu.weibobot.service.RedisService;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import com.scu.weibobot.utils.WeiboOpUtil;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class RedisController {
    @Autowired
    private RedisService redisService;

    @PostMapping("/cache")
    public void addNewCookie(){
        WebDriver driver = WebDriverPool.getWebDriver();
        String username = "13887874056";
        String password = "WEIBOhzh.0123";
        WeiboOpUtil.loginWeibo(driver, username, password);
        Set<Cookie> cookieSet = driver.manage().getCookies();
        WebDriverPool.closeCurrentWebDriver(driver);

        System.out.println(redisService.sSetAndTime("test", 120, cookieSet.toArray()));

        driver = WebDriverPool.getWebDriver();
        driver.get("https://m.weibo.cn");

        for (Object obj : redisService.sGet("test")){
            if (obj instanceof Cookie){
                driver.manage().addCookie((Cookie) obj);
            } else {
                System.out.println("error");
                break;
            }
        }

        driver.navigate().refresh();


    }
}
