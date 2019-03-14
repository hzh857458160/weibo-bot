package com.scu.weibobot.taskexcuter;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.ProxyIp;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.service.ProxyIpService;
import com.scu.weibobot.utils.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class WebDriverPool {
    private static final String DRIVER_PROPERTY = "webdriver.chrome.driver";
    private static final String CHROME_DRIVER_PATH = "C:\\Users\\HanrAx\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe";
    private static ChromeOptions chromeOptions;
    private static int CAPACITY = 50;
    private static AtomicInteger refCount = new AtomicInteger(0);
    private static Map<Long, WebDriver> driverMap = new ConcurrentHashMap<>(CAPACITY);

    @Autowired
    private BotInfoService botInfoService;
    @Autowired
    private ProxyIpService proxyIpService;

    public static WebDriverPool webDriverPool;

    @PostConstruct
    public void init(){
        webDriverPool = this;
        webDriverPool.botInfoService = this.botInfoService;
        webDriverPool.proxyIpService = this.proxyIpService;
    }

    static {
        System.setProperty(DRIVER_PROPERTY, CHROME_DRIVER_PATH);
        initChromeOptions();

    }

    private static void initChromeOptions(){
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
    }

    /**
     * 这个Webdriver目前用在添加新账号
     * 在添加新账号的时候，需要根据随机的地址获取ip
     * 没有ip则，
     * @param location
     * @return
     */
    public static WebDriver getWebDriver(String location){
        log.info("进入getWebDriver({})", location);
        WebDriver driver = null;
        try {
            driver = getWebDriver();
            List<ProxyIp> proxyIpList = ProxyUtil.crawlFrom89IP(driver, location);
            log.info("获取到proxyIpList，size = {}", proxyIpList.size());
            initChromeOptions();
            if (proxyIpList.size() != 0){
                for (ProxyIp proxyIp : proxyIpList){
                    log.info("当前proxyIp为[{}]", proxyIp);
                    if (ProxyUtil.isValid(proxyIp)){
                        log.info("id为{}的代理可用", proxyIp.getId());
                        String ip = proxyIp.getIp() + ":" + proxyIp.getPort();
                        chromeOptions.addArguments("--proxy-server=http://" + ip);
                        break;

                    } else {
                        log.info("id为{}的代理不可用", proxyIp.getId());
                        webDriverPool.proxyIpService.setInvalidProxy(proxyIp.getId());
                    }
                }
            }
            refCount.incrementAndGet();
            return new ChromeDriver(chromeOptions);

        } finally {
            if (driver != null){
                WebDriverPool.closeCurrentWebDriver(driver);
            }
        }

    }
    /**
     * 微博账号专用有代理的driver
     * 获取当前map中的driver
     * @param botInfo
     * @return
     */
    public static WebDriver getWebDriver(BotInfo botInfo){
        log.info("进入getWebDriver({})", botInfo);
        long botId = botInfo.getBotId();
        String location = botInfo.getLocation();
        log.info("获取到botId为{}, location为{}", botId, location);
        if(driverMap.containsKey(botId)){
            log.info("map中有对应的driver");
            refCount.incrementAndGet();
            return driverMap.get(botId);
        }
        List<ProxyIp> proxyIpList = webDriverPool.proxyIpService.findAllByLocation(location);
        log.info("获取到proxyIpList，size = {}", proxyIpList.size());
        initChromeOptions();
        for (ProxyIp proxyIp : proxyIpList){
            log.info("当前proxyIp为[{}]", proxyIp);
            if (ProxyUtil.isValid(proxyIp)){
                log.info("id为{}的代理可用", proxyIp.getId());
                String ip = proxyIp.getIp() + ":" + proxyIp.getPort();
                chromeOptions.addArguments("--proxy-server=http://" + ip);
                break;

            } else {
                log.info("id为{}的代理不可用", proxyIp.getId());
                webDriverPool.proxyIpService.setInvalidProxy(proxyIp.getId());
            }
        }
        WebDriver driver =  new ChromeDriver(chromeOptions);
        refCount.incrementAndGet();
        driverMap.put(botId, driver);
        log.info("当前driverMap的size = {}", driverMap.size());
        return driver;
    }

    /**
     * 普通driver,无代理
     * @return
     */
    public static WebDriver getWebDriver(){
        log.info("进入getWebDriver()");
        refCount.incrementAndGet();
        log.info("Alive driver count is {}", refCount.get());
        initChromeOptions();
        return new ChromeDriver(chromeOptions);
    }


    public static void closeCurrentWebDriver(WebDriver webDriver) {
        refCount.decrementAndGet();
        log.info("Alive driver count is {}", refCount.get());
        webDriver.quit();
    }
}
