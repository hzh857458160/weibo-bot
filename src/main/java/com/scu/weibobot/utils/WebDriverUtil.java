package com.scu.weibobot.utils;

import com.scu.weibobot.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class WebDriverUtil {

    public WebDriverUtil(){}

    @Autowired
    private RedisService redisService;

    public static WebDriverUtil webDriverUtil;

    @PostConstruct
    public void init() {
        webDriverUtil = this;
        webDriverUtil.redisService = this.redisService;
    }

    public static void cacheCurrentIp(String key, String ip) {
        webDriverUtil.redisService.set(key, ip);
    }

    public static String getCachedIp(String key) {
        Object obj = webDriverUtil.redisService.get(key);
        return obj == null ? null : (String) obj;
    }

    public static boolean isCookieExist(String key){
        return webDriverUtil.redisService.hasKey(key);
    }

    public static void saveCurrentCookies(WebDriver driver, String key, long time){
        //将cookie缓存到redis中
        Set<Cookie> cookieSet = getCookies(driver);
        webDriverUtil.redisService.sSetAndTime(key, time, cookieSet.toArray());
    }

    public static boolean getUrlWithCookie(WebDriver driver, String url, String key){
        if (isCookieExist(key)){
            log.info("存在cookies");
            Set<Object> cookieSet = webDriverUtil.redisService.sGet(key);
            driver.get(url);
            waitSeconds(1);
            for (Object obj : cookieSet){
                WebDriverUtil.addCookie(driver, (Cookie) obj);
            }
            driver.navigate().refresh();
            waitSeconds(3);
            log.info("替换原有cookie");
            webDriverUtil.redisService.del(key);
            saveCurrentCookies(driver, key, 24 * 60 * 60);
            return true;
        }

        return false;

    }

    public static WebElement waitUntilElementExist(WebDriver driver, int waitMaxTime, By by) {
        return new WebDriverWait(driver, waitMaxTime)
                .until(ExpectedConditions.visibilityOfElementLocated(by));
    }

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

    public static WebElement forceGetElement(By selector, WebElement element) {
        for (int i = 0; i < 4; i++) {
            try {
                return element.findElement(selector);
            } catch (NoSuchElementException e) {
                waitSeconds(2);
                log.info("尝试重新获取该元素");
            }
        }
        log.error("获取元素失败By:{}，请检查原元素", selector.toString());
        throw new NullPointerException("获取元素失败");

    }


    public static WebElement forceGetElement(By selector, WebDriver driver){
        for (int i = 0; i < 4; i++){
            try {
                return driver.findElement(selector);
            } catch (NoSuchElementException e){
                waitSeconds(2);
                log.info("尝试重新获取该元素");
            }
        }
        log.error("获取元素失败By:{}，请检查当前url：{}", selector.toString(), driver.getCurrentUrl());
        throw new NullPointerException("获取元素失败");

    }


    public static List<WebElement> forceGetElementList(By selector, WebDriver driver){
        for (int i = 0; i < 4; i++){
            try {
                return driver.findElements(selector);
            } catch (NoSuchElementException e){
                waitSeconds(2);
                log.info("尝试重新获取该元素", selector.toString());
            }
        }
        log.error("获取当前元素失败By:{}，请检查当前url：{}", selector.toString(), driver.getCurrentUrl());
        throw new NullPointerException("获取元素失败");
    }

    /**
     * 检查当前元素是否存在
     * @param selector
     * @param obj
     * @return
     */
    public static WebElement isElementExist(By selector, Object obj) {
        try {
            if (obj instanceof WebDriver) {
                return ((WebDriver) obj).findElement(selector);

            }
            if (obj instanceof WebElement) {
                return ((WebElement) obj).findElement(selector);
            }

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
     * 滑动滑动条
     * @param driver
     * @param num
     */
    public static void scrollPage(WebDriver driver, int num) {
        log.info("控制滑动条向下滑动{}像素", num);
        String js1 =  "var top = document.documentElement.scrollTop + " + num
                + "; scrollTo(0, top)";
        String js2 = "document.documentElement.scrollTop = document.documentElement.scrollTop + " + num;
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        if (jsExecutor.executeScript(js1) == null){
            jsExecutor.executeScript(js2);

        }

        waitSeconds(1);
    }

    public static void scrollToElement(WebDriver driver, WebElement element) {
//        log.info("scroll view element");
        WebDriverUtil.jsExecuter(driver, "arguments[0].scrollIntoView(false);", element);
        waitSeconds(1);
    }

    public static void scrollToBottom(WebDriver driver){
        log.info("滑动滚动条到底部");
        scrollPage(driver, 20000);
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

    public static Object jsClick(WebDriver driver, WebElement element) {
        return jsExecuter(driver, "arguments[0].click();", element);
    }

    public static Object jsExecuter(WebDriver driver, String js, Object param){
        return ((JavascriptExecutor)driver).executeScript(js, param);
    }
    public static Object jsExecuter(WebDriver driver, String js){
        return ((JavascriptExecutor)driver).executeScript(js);
    }

    //TODO:将当前截图存入项目目录下，并返回截图地址
    public static String getScreenShotFileName(WebElement element) throws IOException {
        WrapsDriver wrapsDriver = (WrapsDriver) element;
        File screen = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
        BufferedImage image = ImageIO.read(screen);
        //元素坐标
        Point p = element.getLocation();
        //获取元素的高度、宽度
        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        Dimension dimension = new Dimension(width, height);
        //创建一个矩形使用上面的高度，和宽度
        Rectangle rect = new Rectangle(p, dimension);
        BufferedImage img = image.getSubimage(p.getX(), p.getY(), rect.width, rect.height);
        ImageIO.write(img, "png", screen);
        File tempFile = new File("src\\main\\resources\\static\\img\\screenshots\\" + System.currentTimeMillis() + ".png");
        FileUtils.copyFile(screen, tempFile);
        screen.delete();
        return tempFile.getName();
    }

//    public static String getCommentScreenShot(WebDriver driver) throws IOException {
//        WebElement sourceWeibo = WebDriverUtil.forceGetElement(By.cssSelector("div.card.m-panel.card9.f-weibo"), driver);
//        WebElement selfComment = WebDriverUtil.forceGetElementList(
//                By.cssSelector("div.card.m-avatar-box.lite-page-list"), driver).get(0);
//
//        File screen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//        BufferedImage image = ImageIO.read(screen);
//        //元素坐标
//        Point pWeibo = sourceWeibo.getLocation();
//        Point pComment = selfComment.getLocation();
//        //获取元素的高度、宽度
//        int width = sourceWeibo.getSize().getWidth();
//        int height = pComment.getY() + selfComment.getSize().getHeight();
//        Dimension dimension = new Dimension(width, height);
//        //创建一个矩形使用上面的高度，和宽度
//        Rectangle rect = new Rectangle(pWeibo, dimension);
//        BufferedImage img = image.getSubimage(pWeibo.getX(), pWeibo.getY(), rect.width, rect.height);
//        ImageIO.write(img, "png", screen);
//        File tempFile = new File("src\\main\\resources\\static\\img\\screenshots\\" + System.currentTimeMillis() + ".png");
//        FileUtils.copyFile(screen, tempFile);
//        screen.delete();
//        return tempFile.getName();
//    }


}
