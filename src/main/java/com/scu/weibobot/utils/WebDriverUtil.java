package com.scu.weibobot.utils;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;

import java.io.File;
import java.util.List;
import java.util.Set;

@Slf4j
public class WebDriverUtil {

    private WebDriverUtil(){}

    /**
     * 睡眠对应秒数
     * @param second
     */
    public static void waitSeconds(int second){
        waitSeconds((double)second);
    }

    /**
     * 睡眠对应秒数，double
     * @param second
     */
    public static void waitSeconds(double second){
        try {
            Thread.sleep((long) (second * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开一个新tab页，返回该tab页的window handle
     * @param driver
     * @param url
     * @return
     */
    public static String openNewTab(WebDriver driver, String url){
        Set<String> set1 = driver.getWindowHandles();
        ((JavascriptExecutor)driver).executeScript("window.open('" + url + "','_blank');");
        waitSeconds(1);
        Set<String> set2 = driver.getWindowHandles();
        for(String temp : set2){
            if(!set1.contains(temp)){
                return temp;
            }
        }
        return null;
    }

    /**
     * 检查当前元素是否存在
     * @param selector
     * @param driver
     * @return
     */
    public static WebElement isElementExist(By selector, WebDriver driver){
        try {
            return driver.findElement(selector);

        } catch (NoSuchElementException e){
            log.info("该元素不存在，选择器为{}", selector.toString());
        }
        return null;
    }

    /**
     * 检查当前元素列表是否存在
     * @param selector
     * @param driver
     * @return
     */
    public static List<WebElement> isElementsExist(By selector, WebDriver driver){
        try {
            return driver.findElements(selector);

        } catch (NoSuchElementException e){
            log.info("该元素list不存在，选择器为{}", selector.toString());
        }
        return null;
    }

    /**
     * 切换到一个不是当前页面的页面
     * @param driver
     */
    public static void changeWindow(WebDriver driver){
        // 获取当前页面句柄
        String handle = driver.getWindowHandle();
        // 获取所有页面的句柄，并循环判断不是当前的句柄，就做选取switchTo()
        for (String handles : driver.getWindowHandles()) {
            if (handles.equals(handle))
                continue;
            driver.switchTo().window(handles);
        }
    }

    /**
     * 切换到特定句柄页面
     * @param driver
     */
    public static void changeWindowTo(WebDriver driver,String handle){
        for (String tmp : driver.getWindowHandles()) {
            if (tmp.equals(handle)){
                driver.switchTo().window(handle);
                break;
            }
        }
    }

    /**
     * 关闭当前浏览器的当前页面
     * @param driver
     */
    public static void closeCurrentTab(WebDriver driver){
        driver.close();
    }

    /**
     * 滑动滑动条
     * @param driver
     * @param num
     */
    public static void scrollWeibo(WebDriver driver, int num){
        log.info("控制滑动条向下滑动{}像素", num);
        String js1 =  "var top = document.documentElement.scrollTop + " + num
                + "; scrollTo(0, top)";
        String js2 = "document.documentElement.scrollTop = document.documentElement.scrollTop + " + num;
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        if (jsExecutor.executeScript(js1) == null){
            log.info("第一条语句无效");
            jsExecutor.executeScript(js2);

        }

        waitSeconds(1);
    }

    public static void scrollToBottom(WebDriver driver){
        log.info("滑动滚动条到底部");
        scrollWeibo(driver, 20000);
    }

    public static Set<Cookie> getCookies(WebDriver driver){
        return driver.manage().getCookies();
    }

    public static void addCookie(WebDriver driver, Cookie cookie){
        driver.manage().addCookie(cookie);
    }

    public static void addCookies(WebDriver driver, Set<Cookie> cookieSet){
        cookieSet.forEach(cookie -> addCookie(driver, cookie));
        driver.navigate().refresh();
    }

    public static Object jsExecuter(WebDriver driver, String js, Object param){
        return ((JavascriptExecutor)driver).executeScript(js, param);
    }
    public static Object jsExecuter(WebDriver driver, String js){
        return ((JavascriptExecutor)driver).executeScript(js);
    }


    public void getScreenShot(WebDriver driver) {
        if (driver instanceof TakesScreenshot) {
            TakesScreenshot screenshotTaker = (TakesScreenshot) driver;
            File file = screenshotTaker.getScreenshotAs(OutputType.FILE);
        }
    }

}
