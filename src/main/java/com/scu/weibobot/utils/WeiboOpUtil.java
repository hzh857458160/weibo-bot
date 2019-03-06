package com.scu.weibobot.utils;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

@Slf4j
public class WeiboOpUtil {
    private static Set<Cookie> cookieSet = null;
    private static final String BASE_URL = "https://passport.weibo.cn/signin/login";
    private static WebDriver driver = null;


    public static boolean loginWeibo(String username, String password){
        if (driver == null){
            driver = WebDriverUtil.getWebDriver();
        }
//        if (cookieSet != null){
//            cookieSet.forEach(cookie -> driver.manage().addCookie(cookie));
//            cookieSet.forEach(System.out::println);
//            driver.get("https://m.weibo.cn");
//            waitSeconds(4);
//            return true;
//        }
        log.info("登录微博，账号为{}", username);
        driver.get(BASE_URL);
        waitSeconds(3);

        while(true){
            log.info("输入用户名");
            WebElement usernameInput = driver.findElement(By.id("loginName"));
            usernameInput.sendKeys(username);
            waitSeconds(2);

            log.info("输入密码");
            WebElement passwordInput = driver.findElement(By.id("loginPassword"));
            passwordInput.sendKeys(password);
            waitSeconds(2);

            log.info("点击登录");
            WebElement loginBtn = driver.findElement(By.id("loginAction"));
            loginBtn.click();
            waitSeconds(5);

            WebElement errorMsg = WebDriverUtil.isElementExist(By.id("errorMsg"), driver);
            if (errorMsg != null){
                log.info(errorMsg.isDisplayed() + "");
                if (errorMsg.isDisplayed()){
                    log.info("账号密码错误");
                    closeChrome();
                    return false;
                }
            }

            String curUrl = driver.getCurrentUrl();
            log.info(curUrl);
            if (curUrl.equals(BASE_URL)){
                log.info("网页没有跳转，可能是卡住了，重新输入");
                openUrlInNewTab(BASE_URL);
                closeCurrentTab();
                switchToIndexTab(0);

            } else if (curUrl.contains("security.weibo.com/captcha/geetest")){
                log.info("受到微博限制，进入身份验证");
                waitSeconds(3);
                return true;

            } else {
               log.info("成功登陆到m.weibo.cn");
               break;
            }

        }
        cookieSet =  driver.manage().getCookies();
        return true;
    }

    public static void postWeibo(String content){
        waitSeconds(3);
        log.info("发送微博，内容为[{}]", content);
        driver.findElement(By.cssSelector("div.lite-iconf.lite-iconf-releas")).click();
        waitSeconds(1);
        WebElement textarea = driver.findElement(By.cssSelector("span.m-wz-def > textarea"));
        textarea.click();
        waitSeconds(3);
        textarea.sendKeys(content);
        WebElement sendBtn = driver.findElement(By.cssSelector("a.m-send-btn"));
        sendBtn.click();
        waitSeconds(2);
    }

    public static void reportWeibo(int index, String reportContent){
        waitSeconds(3);
        log.info("转发第{}条微博，添加内容为[{}]",index, reportContent);
        //获取想要转发的微博
        WebElement weibo = getFocusWeibo(index);
        //定位转发按钮并点击
        WebElement reportBtn = weibo.findElement(By.cssSelector("i.lite-iconf.lite-iconf-report"));
        reportBtn.click();
        waitSeconds(1);
        //定位输入框并输入内容
        WebElement textarea = driver.findElement(By.cssSelector("span.m-wz-def > textarea"));
        textarea.click();
        waitSeconds(2);
        textarea.sendKeys(reportContent);
        //定位发送按钮并点击
        WebElement sendBtn = driver.findElement(By.cssSelector("a.m-send-btn"));
        sendBtn.click();
        waitSeconds(2);
    }

    public static void commentWeibo(int index, String commentContent){
        waitSeconds(3);
        log.info("评论第{}条微博，评论内容为[{}]",index, commentContent);
        WebElement weibo = getFocusWeibo(index);
        WebElement commentBtn = weibo.findElement(By.cssSelector("i.lite-iconf.lite-iconf-comments"));
        commentBtn.click();
        waitSeconds(1);
        //定位输入框并输入内容
        WebElement textarea = driver.findElement(By.cssSelector("span.m-wz-def > textarea"));
        textarea.click();
        waitSeconds(2);
        textarea.sendKeys(commentContent);
        //定位发送按钮并点击
        WebElement sendBtn = driver.findElement(By.cssSelector("a.m-send-btn"));
        sendBtn.click();
        waitSeconds(1);
    }

    public static void scrollWeibo(int num){
        log.info("控制滑动条向下滑动{}像素", num);
        String js =  "var top = document.documentElement.scrollTop + " + num
                + "; scrollTo(0, top)";
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(js);
        waitSeconds(1);
    }
    public static void scrollWeibo(){
        scrollWeibo(500);
    }

    public static void likeWeibo(int index){
        waitSeconds(3);
        log.info("点赞第{}条微博", index);
        WebElement weibo = getFocusWeibo(index);
        WebElement likeBtn = weibo.findElement(By.cssSelector("i.lite-iconf.lite-iconf-like"));
        likeBtn.click();

    }

    public static void initInfoEdit(){
        By userCenterBy = By.cssSelector("div.nav-left.lite-iconf.lite-iconf-profile");
        WebElement userCenterBtn = WebDriverUtil.isElementExist(userCenterBy, driver);
        if (userCenterBtn == null){
            driver.navigate().refresh();
            waitSeconds(2);
            userCenterBtn = driver.findElement(userCenterBy);
        }
        userCenterBtn.click();
        waitSeconds(2);
        WebElement editDataBtn = driver.findElement(By.cssSelector("div.bar-btn.m-box-col > a"));
        driver.get(editDataBtn.getAttribute("href"));
        waitSeconds(2);
    }

    public static boolean setBotInfo(int gender, int location, LocalDate birthDate){
        try {
            initInfoEdit();

            setLocation(location);
            setGender(gender);
            setBirthDate(birthDate);

            driver.findElement(By.id("save")).click();
            waitSeconds(2);
            closeChrome();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }



    }

    public static boolean setBotInfo(String nickName, int gender, int location, LocalDate birthDate){
        try {
            initInfoEdit();

            setNickName(nickName);
            setLocation(location);
            setGender(gender);
            setBirthDate(birthDate);

            driver.findElement(By.id("save")).click();
            waitSeconds(2);
            closeChrome();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }



    }

    private static void setNickName(String nickName){
        //修改昵称
        openUrlInNewTab("https://m.weibo.cn/setting/nick");
        switchToIndexTab(1);
        waitSeconds(2);
        WebElement nickNameInput = driver.findElement(By.cssSelector("input[placeholder = '请填写昵称']"));
        WebElement ensureBtn = driver.findElement(By.cssSelector("a.m-btn.m-btn-block.m-btn-orange"));

        while(true){
            //输入昵称
            nickNameInput.clear();
            nickNameInput.click();
            waitSeconds(1);
            nickNameInput.sendKeys(nickName);
            log.info("清空输入框并输入昵称:{}", nickName);
            //确认
            ensureBtn.click();
            waitSeconds(2);


            WebElement repeatedTip = WebDriverUtil.isElementExist(By.cssSelector("div.m-alert > header > h3"), driver);
            if (repeatedTip == null){
                closeCurrentTab();
                switchToIndexTab(0);
                break;
            }
            log.info("出现昵称重复提示框");
            WebElement knowBtn = driver.findElement(By.cssSelector("a.m-btn-text-orange"));
            knowBtn.click();


            //因为昵称冲突，需要修改昵称
            nickName = nickName.substring(0, nickName.length() - 2);
            nickName = GenerateInfoUtil.addSuffixToNickName(nickName);
            log.info("重新修改昵称为:{}", nickName);
        }
    }

    private static void setLocation(int province){
        //所在地，下拉框
        Select provinceSelector = new Select(driver.findElement(By.id("province")));
        provinceSelector.selectByIndex(province);
        waitSeconds(2);

        WebElement cityElement = null;
        while(cityElement == null){
            waitSeconds(2);
            cityElement = WebDriverUtil.isElementExist(By.id("city"), driver);
        }
        Select citySelector = new Select(cityElement);
        int cityNum = new Random().nextInt(citySelector.getOptions().size());
        citySelector.selectByIndex(cityNum);
        String city = citySelector.getFirstSelectedOption().getText();
        log.info("{}", province + city);

    }

    private static void setGender(int gender){
        //性别，下拉框
        Select genderSelector = new Select(driver.findElement(By.id("sex")));
        waitSeconds(2);
        genderSelector.selectByIndex(gender);
    }
    private static void setBirthDate(LocalDate birthDate){
        //生日
        Select yearSelector = new Select(driver.findElement(By.id("year")));
        Select monthSelector = new Select(driver.findElement(By.id("month")));
        Select daySelector = new Select(driver.findElement(By.id("day")));
        waitSeconds(2);
        yearSelector.selectByValue(birthDate.getYear() + "");
        waitSeconds(2);
        monthSelector.selectByValue(birthDate.getMonthValue() + "");
        waitSeconds(2);
        daySelector.selectByValue(birthDate.getDayOfMonth() + "");
    }

    public static void closeChrome(){
        driver.quit();
    }

    public static void closeCurrentTab(){
        driver.close();
    }

    private static WebElement getFocusWeibo(int index){
        return driver.findElements(By.cssSelector("div.wb-item-wrap")).get(index);
    }

    public static void openUrlInNewTab(String url){
        ((JavascriptExecutor)driver).executeScript("window.open('" + url + "')");
    }

    public static void switchToIndexTab(int index){
        Object[] objs = driver.getWindowHandles().toArray();
        if (index >= objs.length){
            log.info("没有对应的tab页，无法切换");
            return;
        }
        driver.switchTo().window((String) objs[index]);
    }



}
