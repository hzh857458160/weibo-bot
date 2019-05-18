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
import java.util.Arrays;
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
            waitSeconds(3);
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
            if (handles.equals(handle)) {
                continue;
            }
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

    public static void changeWindow(WebDriver driver, String... handles) {
        List<String> list = Arrays.asList(handles);
        for (String tmp : driver.getWindowHandles()) {
            if (!list.contains(tmp)) {
                driver.switchTo().window(tmp);
                break;
            }
        }
    }

    public static void changeWindowAndCloseOthers(WebDriver driver, String handle) {
        Set<String> handleSet = driver.getWindowHandles();
        if (!driver.getWindowHandle().equals(handle)) {
            driver.switchTo().window(handle);
        }
        for (String tmp : handleSet) {
            if (!tmp.equals(handle)) {
                driver.switchTo().window(tmp);
                driver.close();
                driver.switchTo().window(handle);
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
        WebDriverUtil.jsExecute(driver, "arguments[0].scrollIntoView(false);", element);
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
        return jsExecute(driver, "arguments[0].click();", element);
    }

    public static Object jsExecute(WebDriver driver, String js, Object param) {
        if (param == null) {
            return ((JavascriptExecutor) driver).executeScript(js);
        }
        return ((JavascriptExecutor) driver).executeScript(js, param);
    }

    public static Object jsExecute(WebDriver driver, String js) {
        return jsExecute(driver, js, null);
    }

    public static String screenShot4Common(WebDriver driver, WebElement element) throws IOException {
        scrollToElement(driver, element);
        WrapsDriver wrapsDriver = (WrapsDriver) element;
        File screen = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
        BufferedImage image = ImageIO.read(screen);
        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        int x = element.getLocation().getX();
        int y;
        log.info("元素位置数据：{} + {} ? {}", element.getLocation().getY(), height, image.getHeight());
        if (element.getLocation().getY() + height < image.getHeight()) {
            //说明元素处在中部
            y = element.getLocation().getY();
        } else {
            //说明元素处在底部
            y = image.getHeight() - height;
        }
        return cutAndSaveImage(screen, x, y, width, height);
    }

    public static String screenShot4Comment(WebDriver driver) throws IOException {
        WebElement element = WebDriverUtil.forceGetElement(By.cssSelector("div.lite-page-tab"), driver);
        File screen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage image = ImageIO.read(screen);
        int width = element.getSize().getWidth();
        int height = image.getHeight();
        int x = element.getLocation().getX();
        int y = 0;
        return cutAndSaveImage(screen, x, y, width, height);
    }

    public static String screenShot4InfoSet(WebDriver driver) throws IOException {
        WebElement element = WebDriverUtil.forceGetElement(By.cssSelector("#h5_page_wrap"), driver);
        File screen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        int x = element.getLocation().getX();
        int y = 0;
        return cutAndSaveImage(screen, x, y, width, height);
    }

    private static String cutAndSaveImage(File screen, int x, int y, int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(screen);
        BufferedImage img = image.getSubimage(x, y, width, height);
        ImageIO.write(img, "png", screen);
        File tempFile = new File("src\\main\\resources\\static\\img\\screenshots\\" + System.currentTimeMillis() + ".png");
        FileUtils.copyFile(screen, tempFile);
        screen.delete();
        return tempFile.getName();
    }


}
