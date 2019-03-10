package com.scu.weibobot.taskexcuter;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class WebDriverPool {
    private static final String DRIVER_PROPERTY = "webdriver.chrome.driver";
    private static final String CHROME_DRIVER_PATH = "C:\\Users\\HanrAx\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe";
    private static ChromeOptions chromeOptions;
    private static int CAPACITY = 10;
    private static AtomicInteger refCount = new AtomicInteger(0);

    private static BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<>(CAPACITY);

    static {
        System.setProperty(DRIVER_PROPERTY, CHROME_DRIVER_PATH);
        chromeOptions = new ChromeOptions();
//        chromeOptions.setHeadless(true);
        //浏览器开启即最大化
        chromeOptions.addArguments("--disable-plugins","--disable-images","--start-maximized", "disable-infobars");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("profile.default_content_setting_values.notifications", 2); //禁用浏览器弹窗
        prefs.put("profile.managed_default_content_settings.images", 2); //禁止下载加载图片
        // 禁止加载js
        prefs.put("profile.default_content_settings.javascript", 2); // 2就是代表禁止加载的意思

        chromeOptions.setExperimentalOption("prefs", prefs);

        /* Add the WebDriver proxy capability.
        Proxy proxy = new Proxy();
        proxy.setHttpProxy("myhttpproxy:3337");
        capabilities.setCapability("proxy", proxy);
        */
    }

    private WebDriverPool() {
    }
    public static void initWebDriverPool(int poolSize){
        CAPACITY = poolSize;
        innerQueue = new LinkedBlockingDeque<>(poolSize);
    }

    public static WebDriver getWebDriver() throws InterruptedException {
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (refCount.get() < CAPACITY) {
            synchronized (innerQueue) {
                if (refCount.get() < CAPACITY) {
                    WebDriver driver = new ChromeDriver(chromeOptions);
                    driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                    innerQueue.add(driver);
                    refCount.incrementAndGet();
                }
            }
        }
        log.info("当前引用数量为{}, 当前队列大小为{}", refCount.get(), innerQueue.size());
        return innerQueue.take();
    }

    public static void closeAndReturnToPool(WebDriver webDriver) {
        closeCurrentWebDriver(webDriver);
        innerQueue.add(webDriver);
        log.info("当前引用数量为{}, 当前队列大小为{}", refCount.get(), innerQueue.size());
    }

    public static void closeCurrentWebDriver(WebDriver webDriver) {
        refCount.decrementAndGet();
        webDriver.quit();
    }

    public static void shutdown() {
        try {
            for (WebDriver driver : innerQueue) {
                closeCurrentWebDriver(driver);
            }
            innerQueue.clear();
        } catch (Exception e) {
            // e.printStackTrace();
            log.warn("webdriverpool关闭失败", e);
        }
    }
}
