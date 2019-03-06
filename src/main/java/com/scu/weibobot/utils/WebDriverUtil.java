package com.scu.weibobot.utils;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

@Slf4j
public class WebDriverUtil {
    private static final String DRIVER_PROPERTY = "webdriver.chrome.driver";
    private static final String CHROME_DRIVER_PATH = "C:\\Users\\HanrAx\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe";
    private static ChromeOptions chromeOptions = new ChromeOptions();
    private static final String MOBILE_USER_AGENT = "user-agent = 'Mozilla/5.0 (Linux; U; Android 8.1.0; zh-CN; BLA-AL00 Build/HUAWEIBLA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/11.9.4.974 UWS/2.13.1.48 Mobile Safari/537.36 AliApp(DingTalk/4.5.11) com.alibaba.android.rimet/10487439 Channel/227200 language/zh-CN'";


    static {
        System.setProperty(DRIVER_PROPERTY, CHROME_DRIVER_PATH);
//        chromeOptions.setHeadless(true);
        //浏览器开启即最大化
        chromeOptions.addArguments("--disable-plugins","--disable-images","--start-maximized");
        //使用移动端user-agent
//        chromeOptions.addArguments(MOBILE_USER_AGENT);

    }


    public static WebDriver getWebDriver(){
        log.info("返回一个driver");
        return new ChromeDriver(chromeOptions);
    }

    public static void waitSeconds(int second){
        waitSeconds((double)second);
    }

    public static void waitSeconds(double second){
        try {
            Thread.sleep((long) (second * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static WebElement isElementExist(By selector, WebDriver driver){
        try {
            return driver.findElement(selector);

        } catch (NoSuchElementException e){
            log.info("该元素不存在，选择器为{}", selector.toString());
        }
        return null;
    }

    public static List<WebElement> isElementsExist(By selector, WebDriver driver){
        try {
            return driver.findElements(selector);

        } catch (NoSuchElementException e){
            log.info("该元素list不存在，选择器为{}", selector.toString());
        }
        return null;
    }

    public static void switchToCurrentPage(WebDriver driver) {
        String handle = driver.getWindowHandle();
        for (String tempHandle : driver.getWindowHandles()) {
           if(tempHandle.equals(handle)) {
                driver.close();
           }else {
                driver.switchTo().window(tempHandle);
           }
       }
    }
}
