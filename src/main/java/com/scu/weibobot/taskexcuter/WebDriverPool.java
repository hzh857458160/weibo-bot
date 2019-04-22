package com.scu.weibobot.taskexcuter;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.pojo.ProxyIp;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.utils.PropertiesUtil;
import com.scu.weibobot.utils.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class WebDriverPool {

//    private static String DRIVER_PROPERTY = "webdriver.chrome.driver";
//    private static String CHROME_DRIVER_PATH = "D:\\chrome-test\\Chrome-bin\\chromedriver.exe";

    private static int CAPACITY = 3;
    private static AtomicInteger refCount = new AtomicInteger(0);
    private static Semaphore botSemaphore = new Semaphore(CAPACITY);
    //    private static Semaphore proxySemaphore = new Semaphore(1);
    @Autowired
    private BotInfoService botInfoService;

    public static WebDriverPool webDriverPool;

    @PostConstruct
    public void init(){
        webDriverPool = this;
        webDriverPool.botInfoService = this.botInfoService;
    }

    static {
        PropertiesUtil.loadAllProperties("config.properties");
        String driverProperty = PropertiesUtil.getProperty("DRIVER_PROPERTY");
        String chromeDriverPath = PropertiesUtil.getProperty("CHROME_DRIVER_PATH");
        System.setProperty(driverProperty, chromeDriverPath);
    }

    private static ChromeOptions getInitChromeOptions(){
        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.setHeadless(true);
        //浏览器开启即最大化
        chromeOptions.addArguments("--disable-plugins","--disable-images","--start-maximized", "disable-infobars");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("profile.default_content_setting_values.notifications", 2); //禁用浏览器弹窗
        prefs.put("profile.managed_default_content_settings.images", 2); //禁止下载加载图片
        // 禁止加载js
        prefs.put("profile.default_content_settings.javascript", 2); // 2就是代表禁止加载的意思
        prefs.put("excludeSwitches", Collections.singletonList("enable-automation"));
        chromeOptions.setExperimentalOption("prefs", prefs);
        return chromeOptions;
    }

    /**
     * 这个Webdriver目前用在添加新账号
     * 在添加新账号的时候，需要根据随机的地址获取ip
     * 没有ip则，
     * @param province
     * @return
     */
    public static WebDriver getWebDriver(String province) {
        ChromeOptions options = getInitChromeOptions();
        try {
            log.info("尝试获取信号量");
            botSemaphore.acquire();
            log.info("获取信号量成功");
            log.info("进入getWebDriver({})", province);
            if (province != null) {
//                proxySemaphore.acquire();
                //TODO：当不存在当前区域的代理时的处理
                ProxyIp proxyIp = ProxyUtil.getOneProxyWithProvince(province);
//                proxySemaphore.release();
                if (proxyIp == null) {
                    botSemaphore.release();
                    return null;
                }
                log.info("获取到{}的代理为：{}", province, proxyIp.getLocation());
                options.addArguments("--proxy-server=http://" + proxyIp.getIp() + ":" + proxyIp.getPort());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        refCount.incrementAndGet();
        log.info("Alive driver count is {}", refCount.get());
        return new ChromeDriver(options);
    }



    /**
     * 微博账号专用有代理的driver
     * 获取当前map中的driver
     * @param botInfo
     * @return
     */
    public static WebDriver getWebDriver(BotInfo botInfo){
        String province = getProvinceFromLocation(botInfo.getLocation());
       return getWebDriver(province);
    }

    /**
     * 普通driver,无代理
     * @return
     */
    public static WebDriver getWebDriver(){
        return new ChromeDriver(getInitChromeOptions());
    }


    public static void closeWebDriver(WebDriver webDriver) {
        if (webDriver != null){
            log.info("释放信号量");
            botSemaphore.release();
            refCount.decrementAndGet();
            log.info("Alive driver count is {}", refCount.get());
            webDriver.quit();
        }

    }

    private static String getProvinceFromLocation(String location) {
        //解析出省会
        String province;
        if (location.contains("内蒙古")) {
            province = "内蒙古";
        } else if (location.contains("黑龙江")) {
            province = "黑龙江";
        } else {
            province = location.substring(0, 2);
        }

        return province;
    }
}
