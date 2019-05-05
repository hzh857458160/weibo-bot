package com.scu.weibobot.utils;

import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.pojo.WeiboUser;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;


@Slf4j
public class WeiboOpUtil {
    private static final String LOGIN_URL = "https://passport.weibo.cn/signin/login";
    private static final String BASE_URL = "https://m.weibo.cn";
    private static final int SECONDS_OF_ONE_DAY = 24 * 60 * 60;

    private static final String LIKE_BTN_SELECTOR = "i.lite-iconf.lite-iconf-like";
    private static final String COMMENT_BTN_SELECTOR = "i.lite-iconf.lite-iconf-comments";
    private static final String REPORT_BTN_SELECTOR = "i.lite-iconf.lite-iconf-report";


    public static boolean loginWeibo(WebDriver driver, WeiboAccount account) {
        return loginWeibo(driver, account.getUsername(), account.getPassword());
    }
    /**
     * 登陆微博
     * @param driver
     * @param username
     * @param password
     * @return
     */
    public static boolean loginWeibo(WebDriver driver, String username, String password) {
        log.info("查看是否存有cookie");
        if (WebDriverUtil.getUrlWithCookie(driver, BASE_URL, username)) {
            return true;
        }
        log.info("登录微博，账号为{}", username);
        driver.get(LOGIN_URL);

        log.info("输入用户名");
        WebElement usernameInput = WebDriverUtil.waitUntilElementExist(driver, 10, By.id("loginName"));
        usernameInput.click();
        usernameInput.sendKeys(username);
        waitSeconds(3);

        log.info("输入密码");
        WebElement passwordInput = WebDriverUtil.forceGetElement(By.id("loginPassword"), driver);
        passwordInput.click();
        passwordInput.sendKeys(password);
        waitSeconds(3);

        log.info("点击登录");
        WebElement loginBtn = WebDriverUtil.forceGetElement(By.id("loginAction"), driver);
        loginBtn.click();
        waitSeconds(5);

        WebElement errorMsg = WebDriverUtil.isElementExist(By.id("errorMsg"), driver);
        if (errorMsg != null) {
            log.info(errorMsg.isDisplayed() + "");
            if (errorMsg.isDisplayed()) {
                log.info("账号密码错误");
                return false;
            }
        }

        String secureUrl = "security.weibo.com/captcha/geetest";
        while (driver.getCurrentUrl().contains(secureUrl)) {
            log.info("受到微博限制，进入身份验证");
            WebElement checkBtn = WebDriverUtil.forceGetElement(By.id("embed-captcha"), driver);
            Actions actions = new Actions(driver);
            actions.moveToElement(checkBtn);
            actions.click();
            waitSeconds(8);
        }

        log.info("成功登陆到m.weibo.cn");
        WebDriverUtil.saveCurrentCookies(driver, username, SECONDS_OF_ONE_DAY);
        return true;
    }

    /**
     * 发布微博
     * @param driver
     * @param content
     */
    public static void postWeibo(WebDriver driver, String content, boolean addEmote) throws IOException {
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
        WebDriverUtil.forceGetElement(By.cssSelector(LIKE_BTN_SELECTOR), weibo).click();

//        return getWeiboName(weibo);
    }

    public static void commentWeibo(WebDriver driver, WebElement weibo, String content) {
        log.info("评论微博:{}", content);
        String text = WebDriverUtil.forceGetElement(By.cssSelector("footer > div:nth-child(2) > h4"), weibo).getText();
        WebDriverUtil.forceGetElement(By.cssSelector(COMMENT_BTN_SELECTOR), weibo).click();
        waitSeconds(2);
        if ("评论".equals(text)) {
            inputContentAndSubmit(driver, content);
            weibo.click();

        } else {
            WebDriverUtil.forceGetElement(By.cssSelector("div.box-left.m-box-col.m-box-center-a"), driver).click();
            WebElement textarea = WebDriverUtil.forceGetElement(By.cssSelector("textarea.textarea"), driver);
            textarea.sendKeys(content);
            WebDriverUtil.forceGetElement(By.cssSelector("button.btn-send"), driver).click();
            waitSeconds(2);
            WebDriverUtil.forceGetElement(By.cssSelector("div.nav-left"), driver).click();
        }

    }

    public static void reportWeibo(WebDriver driver, WebElement weibo) {
        reportWeibo(driver, weibo, "");
    }

    public static void reportWeibo(WebDriver driver, WebElement weibo, String content) {
        try {
            log.info("转发微博:{}", content);
            WebDriverUtil.forceGetElement(By.cssSelector(REPORT_BTN_SELECTOR), weibo).click();
            inputContentAndSubmit(driver, content);

        } catch (WebDriverException e) {
            if (e.getMessage().contains("not clickable at")) {
                WebDriverUtil.scrollPage(driver, 200);
                reportWeibo(driver, weibo, content);
            }
        }


    }

    public static void backToMainPage(WebDriver driver) {
        WebDriverUtil.forceGetElement(By.cssSelector("div.nav-left"), driver).click();
        waitSeconds(2);
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

    public static List<WeiboUser> subscribeWeiboByInterest(WebDriver driver, List<String> interestList) {
        String handle = WebDriverUtil.openNewTab(driver, BASE_URL);
        WebDriverUtil.changeWindowTo(driver, handle);
        if (interestList == null) {
            log.error("list == null");
            throw new RuntimeException("interestList is null");
        }
        if (interestList.size() == 0) {
            log.error("list size = 0");
            throw new RuntimeException("interestList size is 0");
        }
        log.info("当前url为：{}", driver.getCurrentUrl());
        //先点击一次搜索框，转到搜索页
        WebElement preSearchBar = WebDriverUtil.forceGetElement(By.cssSelector("label.m-search"), driver);
        preSearchBar.click();
        while (!driver.getCurrentUrl().contains("search?containerid")) {
            waitSeconds(2);
        }
        WebElement searchBar;
        List<WeiboUser> weiboUserList = new ArrayList<>(200);
        for (String interest : interestList) {
            //再点击一次搜索框，并输入相关爱好，并回车
            searchBar = WebDriverUtil.forceGetElement(By.cssSelector("input[type = 'search']"), driver);
            searchBar.clear();
            searchBar.click();
            searchBar.sendKeys(interest);
            searchBar.sendKeys(Keys.ENTER);
            waitSeconds(3);

            //点击用户标签页，等待跳转
            WebDriverUtil.forceGetElement(By.cssSelector("ul.nav-item.center > li:nth-child(2)"), driver).click();
            waitSeconds(3);

            List<WebElement> subscribeList = WebDriverUtil.forceGetElementList(
                    By.cssSelector("div.card-main"), driver);
            String jsClick = "arguments[0].click();";
            for (int i = 1; i <= 25; i++) {
                if (subscribeList.size() == i) {
                    subscribeList = WebDriverUtil.forceGetElementList(By.cssSelector("div.card-main"), driver);
                }
                WebElement tempElement = subscribeList.get(i);
                WebDriverUtil.scrollToElement(driver, tempElement);
                String fansNum = WebDriverUtil.forceGetElement(
                        By.cssSelector("div.m-text-box > h4:nth-child(3)"), tempElement).getText();
                if (!fansNum.contains("万")) {
                    waitSeconds(1);
                    continue;
                }
                WebElement addBox = WebDriverUtil.isElementExist(By.cssSelector("div.m-add-box"), tempElement);
                if (addBox == null) {
                    waitSeconds(1);
                    continue;
                }
                String addTip = WebDriverUtil.forceGetElement(By.cssSelector("h4"), addBox).getText();
                if (addTip.equals("加关注")) {
                    WebDriverUtil.jsExecuter(driver, jsClick, addBox);
                    waitSeconds(1);
                }
                String imgSrc = WebDriverUtil.forceGetElement(By.cssSelector("div.m-img-box > img")
                        , tempElement).getAttribute("src");
                String nickName = WebDriverUtil.forceGetElement(By.cssSelector("div.m-box-col.m-box-dir.m-box-center > div > h3 > span")
                        , tempElement).getText();
                String intro = WebDriverUtil.forceGetElement(By.cssSelector("div.m-box-col.m-box-dir.m-box-center > div > h4:nth-child(2)")
                        , tempElement).getText();
                WeiboUser weiboUser = new WeiboUser(imgSrc, nickName, intro);
                weiboUserList.add(weiboUser);
            }
        }

        return weiboUserList;
    }

    public static WebElement getRecentPostWeibo(WebDriver driver) {
        log.info(driver.getCurrentUrl());
        WebElement element = WebDriverUtil.forceGetElement(By.cssSelector("div.nav-left.lite-iconf.lite-iconf-profile"), driver);
        WebDriverUtil.jsClick(driver, element);
        waitSeconds(3);
        WebElement selfWeibo = WebDriverUtil.forceGetElementList(By.cssSelector("div.wb-item-wrap"), driver).get(0);
        WebDriverUtil.scrollToElement(driver, selfWeibo);
        return selfWeibo;
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
        WebDriverUtil.waitUntilElementExist(driver, 5, By.id("J_name"));
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

    /**
     * 设置头像
     *
     * @param driver
     * @param imgPath 绝对路径
     */
    public static void setHeadImg(WebDriver driver, String imgPath) {
        log.info(driver.getCurrentUrl());
        String handle = WebDriverUtil.openNewTab(driver, "https://m.weibo.cn/home/version?url=wap");
        WebDriverUtil.changeWindowTo(driver, handle);
        waitSeconds(3);
        WebDriverUtil.forceGetElement(By.cssSelector("body > div.u > div.ut > a:nth-child(2)"), driver).click();
        waitSeconds(3);
        WebDriverUtil.forceGetElement(By.cssSelector(" a > img[alt = '头像']"), driver).click();
        waitSeconds(3);
        WebElement imgInput = WebDriverUtil.forceGetElement(By.cssSelector("input[type='file']"), driver);
        imgInput.sendKeys(imgPath);
        WebDriverUtil.forceGetElement(By.cssSelector("input[type=\"submit\"]"), driver).click();
        waitSeconds(5);
        driver.close();
    }


    public static List<WeiboUser> getSubscribeList(WebDriver driver) {
        log.info(driver.getCurrentUrl());
        WebDriverUtil.forceGetElement(By.cssSelector("div.nav-left.lite-iconf.lite-iconf-profile"), driver).click();
        waitSeconds(3);
        WebDriverUtil.forceGetElement(By.cssSelector("div.m-box-center-a > span:nth-child(2)"), driver).click();
        waitSeconds(3);
        List<WeiboUser> list = new ArrayList<>(100);
        for (int i = 0; i < 3; i++) {
            WebDriverUtil.scrollToBottom(driver);
            waitSeconds(1);
        }
        List<WebElement> subscribeList = WebDriverUtil.forceGetElementList(By.cssSelector("div.card.m-panel.card28.m-avatar-box"), driver);
        for (WebElement element : subscribeList) {
            String nickName = WebDriverUtil.forceGetElement(By.cssSelector(
                    " div.m-box-col.m-box-dir.m-box-center > div > h3 > span"), element).getText();
            String headImg = WebDriverUtil.forceGetElement(By.cssSelector(
                    "div.m-img-box > img"), element).getAttribute("src");
            String intro = WebDriverUtil.forceGetElement(By.cssSelector(
                    "div.m-box-col.m-box-dir.m-box-center > div > h4:nth-child(2)"), element).getText();
            WeiboUser weiboUser = new WeiboUser();
            weiboUser.setHeadImg(headImg);
            weiboUser.setIntro(intro);
            weiboUser.setNickName(nickName);
            list.add(weiboUser);
        }
        return list;
    }


}
