package com.scu.weibobot.utils;

import com.scu.weibobot.domain.consts.Consts;
import com.scu.weibobot.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;


import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;


@Slf4j
public class WeiboOpUtil {
    private static final String LOGIN_URL = "https://passport.weibo.cn/signin/login";
    private static final String BASE_URL = "https://m.weibo.cn";
    private static final int SECONDS_OF_ONE_DAY = 24 * 60 * 60;

    private static final String LIKE_BTN_SELECTOR = "i.lite-iconf.lite-iconf-like";
    private static final String COMMENT_BTN_SELECTOR = "i.lite-iconf.lite-iconf-comments";
    private static final String REPORT_BTN_SELECTOR = "i.lite-iconf.lite-iconf-report";


    /**
     * 登陆微博
     * @param driver
     * @param username
     * @param password
     * @return
     */
    public static boolean loginWeibo(WebDriver driver, String username, String password) {
        log.info("查看是否存有cookie");
        if (WebDriverUtil.getUrlWithCookie(driver, BASE_URL, username)){
            return true;
        }
        log.info("登录微博，账号为{}", username);
        driver.get(LOGIN_URL);
        WebDriverUtil.waitUntilElement(driver, By.id("loginName"));

        while(true){
            log.info("输入用户名");
            WebElement usernameInput = WebDriverUtil.forceGetElement(By.id("loginName"), driver);
            usernameInput.sendKeys(username);
            waitSeconds(2);

            log.info("输入密码");
            WebElement passwordInput = WebDriverUtil.forceGetElement(By.id("loginPassword"), driver);
            passwordInput.sendKeys(password);
            waitSeconds(2);

            log.info("点击登录");
            WebElement loginBtn = WebDriverUtil.forceGetElement(By.id("loginAction"), driver);
            loginBtn.click();
            waitSeconds(5);

            WebElement errorMsg = WebDriverUtil.isElementExist(By.id("errorMsg"), driver);
            if (errorMsg != null){
                log.info(errorMsg.isDisplayed() + "");
                if (errorMsg.isDisplayed()){
                    log.info("账号密码错误");
                    return false;
                }
            }

            String secureUrl = "security.weibo.com/captcha/geetest";
            while(driver.getCurrentUrl().contains(secureUrl)){
                log.info("受到微博限制，进入身份验证");
                waitSeconds(8);
            }


            String curUrl = driver.getCurrentUrl();
            if (curUrl.equals(LOGIN_URL)){
                log.info("网页没有跳转，可能是卡住了，重新输入");
                driver.navigate().refresh();

            } else {
               log.info("成功登陆到m.weibo.cn");
               break;
            }

        }

        WebDriverUtil.saveCurrentCookies(driver, username, SECONDS_OF_ONE_DAY);
        return true;
    }

    /**
     * 发布微博
     * @param driver
     * @param content
     */
    public static void postWeibo(WebDriver driver, String content, boolean addEmote) {
        waitSeconds(3);
        log.info("发送微博，内容为[{}]", content);
        WebDriverUtil.forceGetElement(By.cssSelector("div.lite-iconf.lite-iconf-releas"), driver).click();
        waitSeconds(1);
        //定位输入框并输入内容
        WebElement textarea = WebDriverUtil.forceGetElement(By.cssSelector("span.m-wz-def > textarea"), driver);
        textarea.click();
        if (addEmote) {
            WebDriverUtil.forceGetElement(By.cssSelector("h4.lite-iconf.lite-iconf-emote"), driver).click();
            List<WebElement> emoteList = WebDriverUtil.forceGetElementList(
                    By.cssSelector("div.m-box-center.m-box-center-a.face-wrap.default"), driver);
            int ra = new Random().nextInt(21);
            emoteList.get(ra).click();
        }
        waitSeconds(2);
        textarea.sendKeys(content);
        //定位发送按钮并点击
        WebElement sendBtn = driver.findElement(By.cssSelector("a.m-send-btn"));
        sendBtn.click();
        waitSeconds(1);
    }

    public static List<WebElement> getWeiboList(WebDriver driver) {
        return WebDriverUtil.forceGetElementList(By.cssSelector("div.wb-item-wrap"), driver);
    }

    public static String getWeiboName(WebElement weibo) {
        return WebDriverUtil.forceGetElement(By.cssSelector("h3.m-text-cut"), weibo).getText();
    }

    public static void likeWeibo(WebElement weibo) {
        log.info("点赞微博");
        WebDriverUtil.forceGetElement(By.cssSelector(LIKE_BTN_SELECTOR), weibo);
//        return getWeiboName(weibo);
    }

    public static void commentWeibo(WebDriver driver, WebElement weibo, String content) {
        log.info("评论微博:{}", content);
        String text = WebDriverUtil.forceGetElement(By.cssSelector("footer > div:nth-child(2) > h4"), weibo).getText();
        WebDriverUtil.forceGetElement(By.cssSelector(COMMENT_BTN_SELECTOR), weibo).click();
        waitSeconds(2);
        if ("评论".equals(text)) {
            inputContentAndSubmit(driver, content);
        } else {
            WebDriverUtil.forceGetElement(By.cssSelector("div.box-left.m-box-col.m-box-center-a"), driver).click();
            WebElement textarea = WebDriverUtil.forceGetElement(By.cssSelector("textarea.textarea"), driver);
            textarea.sendKeys(content);
            WebDriverUtil.forceGetElement(By.cssSelector("button.btn-send"), driver).click();
            waitSeconds(2);
            WebDriverUtil.forceGetElement(By.cssSelector("div.nav-left"), driver).click();
        }
//        return getWeiboName(weibo);

    }

    public static void reportWeibo(WebDriver driver, WebElement weibo) {
        reportWeibo(driver, weibo, "");
    }

    public static void reportWeibo(WebDriver driver, WebElement weibo, String content) {
        log.info("转发微博:{}", content);
        WebDriverUtil.forceGetElement(By.cssSelector(REPORT_BTN_SELECTOR), weibo).click();
        inputContentAndSubmit(driver, content);
//        return getWeiboName(weibo);
    }

    public static void scrollToElement(WebDriver driver, WebElement element) {
        log.info("scroll view element");
        WebDriverUtil.jsExecuter(driver, "arguments[0].scrollIntoView(true);", element);
        waitSeconds(1);
    }

    /**
     * 转发对应的微博
     * @param driver
     * @param index
     * @param reportContent
     */
    public static void reportWeibo(WebDriver driver, int index, String reportContent){
        waitSeconds(3);
        log.info("转发第{}条微博，添加内容为[{}]",index, reportContent);
        //获取想要转发的微博
        WebElement weibo = getFocusWeibo(driver, index);
        //定位转发按钮并点击
        WebElement reportBtn = weibo.findElement(By.cssSelector("i.lite-iconf.lite-iconf-report"));
        reportBtn.click();
        waitSeconds(1);
        inputContentAndSubmit(driver, reportContent);
    }

    /**
     * 评论对应微博
     * @param driver
     * @param index
     * @param commentContent
     */
    public static void commentWeibo(WebDriver driver, int index, String commentContent){
        waitSeconds(3);
        log.info("评论第{}条微博，评论内容为[{}]",index, commentContent);
        WebElement weibo = getFocusWeibo(driver, index);
        WebElement commentBtn = weibo.findElement(By.cssSelector("i.lite-iconf.lite-iconf-comments"));
        commentBtn.click();
        waitSeconds(1);
        inputContentAndSubmit(driver, commentContent);
    }

    private static void inputContentAndSubmit(WebDriver driver, String content){
        //定位输入框并输入内容
        WebElement textarea = WebDriverUtil.forceGetElement(By.cssSelector("span.m-wz-def > textarea"), driver);
        textarea.click();
        waitSeconds(2);
        textarea.sendKeys(content);
        //定位发送按钮并点击
        WebDriverUtil.forceGetElement(By.cssSelector("a.m-send-btn"), driver).click();
        waitSeconds(1);
    }
    /**
     * 点赞对应微博
     * @param driver
     * @param index 从0开始计数
     */
    public static void likeWeibo(WebDriver driver, int index){
        waitSeconds(3);
        log.info("点赞第{}条微博", index);
        WebElement weibo = getFocusWeibo(driver, index);
        WebElement likeBtn = weibo.findElement(By.cssSelector("i.lite-iconf.lite-iconf-like"));
        likeBtn.click();
    }

    public static boolean subscribeWeiboByInterest(WebDriver driver, List<String> interestList){
        try {
            String handle = WebDriverUtil.openNewTab(driver, BASE_URL);
            WebDriverUtil.changeWindowTo(driver, handle);
            if (interestList == null){
                log.error("list == null");
                return false;
            }

            if (interestList.size() == 0){
                log.error("list size = 0");
                return false;
            }
            log.info("当前url为：{}", driver.getCurrentUrl());
            //先点击一次搜索框，转到搜索页
            WebElement preSearchBar =  WebDriverUtil.forceGetElement(By.cssSelector("label.m-search"), driver);
            preSearchBar.click();
            while(!driver.getCurrentUrl().contains("search?containerid")){
                waitSeconds(2);
            }
            for (String interest : interestList) {
                //再点击一次搜索框，并输入相关爱好，并回车
                WebElement searchBar = WebDriverUtil.forceGetElement(By.cssSelector("input[type = 'search']"), driver);
                searchBar.clear();
                searchBar.click();
                searchBar.sendKeys(interest);
                searchBar.sendKeys(Keys.ENTER);
                waitSeconds(3);

                //点击用户标签页，等待跳转
                WebDriverUtil.forceGetElement(By.cssSelector("ul.nav-item.center > li:nth-child(2)"), driver).click();
                waitSeconds(3);
                //获取到可关注按钮（直接获取到当前所有可关注的按钮的list）
                //TODO:随便加个需求，只关注粉丝不低于1万的博主
                List<WebElement> followList =  WebDriverUtil.forceGetElementList(By.cssSelector("i.m-font.m-font-follow"), driver);
                String jsClick = "arguments[0].click();";
                for(int i = 0; i < followList.size(); i++){
                    if ((i + 1) % 5 == 0){
                        WebDriverUtil.scrollWeibo(driver, 550);
                    }
                    WebDriverUtil.jsExecuter(driver, jsClick, followList.get(i));
                    waitSeconds(1);
                }
            }

            return true;

        } catch (Exception e){
            e.printStackTrace();

        } finally {
            WebDriverUtil.changeWindow(driver);
        }

        return false;
    }

    /**
     * 初始化编辑资料界面
     * @param driver
     */
    private static void initInfoEdit(WebDriver driver){
        if (driver.getCurrentUrl().contains("m.weibo.cn/users/")){
            return;
        }
        By userCenterBy = By.cssSelector("div.nav-left.lite-iconf.lite-iconf-profile");
        WebElement userCenterBtn = WebDriverUtil.forceGetElement(userCenterBy, driver);
        userCenterBtn.click();
        waitSeconds(2);
        WebElement editDataBtn = driver.findElement(By.cssSelector("div.bar-btn.m-box-col > a"));
        driver.get(editDataBtn.getAttribute("href"));
        waitSeconds(2);
    }

    public static void setNickName(WebDriver driver, String nickName){
        initInfoEdit(driver);
        //修改昵称
        String handle = WebDriverUtil.openNewTab(driver, "https://m.weibo.cn/setting/nick");
        WebDriverUtil.changeWindowTo(driver, handle);
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
                WebDriverUtil.changeWindow(driver);
                break;
            }
            log.info("出现昵称重复提示框");
            By knowBtnBy = By.cssSelector("a.m-btn-text-orange");
            WebDriverUtil.forceGetElement(knowBtnBy, driver).click();
            waitSeconds(2);

            //因为昵称冲突，需要修改昵称
            nickName = GenerateInfoUtil.reAddSuffixToNickName(nickName);
            log.info("重新修改昵称为:{}", nickName);
        }
    }

    public static String setLocation(WebDriver driver, int province){
        initInfoEdit(driver);
        //所在地，下拉框
        Select provinceSelector = new Select(driver.findElement(By.id("province")));
        provinceSelector.selectByIndex(province);
        waitSeconds(2);

        WebElement cityElement = WebDriverUtil.forceGetElement(By.id("city"), driver);
        Select citySelector = new Select(cityElement);
        int cityNum = new Random().nextInt(citySelector.getOptions().size());
        citySelector.selectByIndex(cityNum);
        String city = citySelector.getFirstSelectedOption().getText();
        log.info("{}", province + city);

        return Consts.PROVINCE[province] + city;
    }

    public static void setGender(WebDriver driver,int gender){
        initInfoEdit(driver);
        //性别，下拉框
        Select genderSelector = new Select(driver.findElement(By.id("sex")));
        waitSeconds(2);
        genderSelector.selectByIndex(gender);
    }


    public static void setBirthDate(WebDriver driver, LocalDate birthDate){
        initInfoEdit(driver);
        //生日
        Select yearSelector = new Select(driver.findElement(By.id("year")));
        Select monthSelector = new Select(driver.findElement(By.id("month")));
        Select daySelector = new Select(driver.findElement(By.id("day")));
        waitSeconds(2);
        yearSelector.selectByValue(birthDate.getYear() + "");
        waitSeconds(2);
        monthSelector.selectByValue(birthDate.getMonthValue() + "");
        waitSeconds(2);
        daySelector.selectByValue((birthDate.getDayOfMonth() - 1) + "");
    }

    public static void saveUserSetting(WebDriver driver){
        WebElement saveBtn = WebDriverUtil.forceGetElement(By.id("save"), driver);
        saveBtn.click();
        waitSeconds(3);
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException e){
            log.warn("没有弹窗");
        }
    }

    private static WebElement getFocusWeibo(WebDriver driver, int index){
        return driver.findElements(By.cssSelector("div.wb-item-wrap")).get(index);
    }

    //TODO:尚未测试该逻辑，还可以使用Robot类手动操作
    public static void setHeadImg(WebDriver driver, String imgPath) {
        driver.get("https://weibo.com");
        WebDriverUtil.forceGetElement(By.cssSelector("div.headpic"), driver).click();
        WebDriverUtil.forceGetElement(By.cssSelector("div.pf_photo"), driver).click();
        WebElement imgInput = WebDriverUtil.forceGetElement(By.cssSelector("input[name='pic1']"), driver);
        imgInput.sendKeys(imgPath);
        imgInput.submit();

    }


}
