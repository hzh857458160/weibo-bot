package com.scu.weibobot.utils;

import com.scu.weibobot.domain.Consts;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;
import java.util.*;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

@Slf4j
public class WeiboOpUtil {
    private static final String LOGIN_URL = "https://passport.weibo.cn/signin/login";
    private static final String BASE_URL = "https://m.weibo.cn";
    private static Map<String, Set<Cookie>> cookieMap = new HashMap<>(20);

    /**
     * 登陆微博
     * @param driver
     * @param username
     * @param password
     * @return
     */
    public static boolean loginWeibo(WebDriver driver, String username, String password) {
        boolean flag = cookieMap.containsKey(username);
        log.info("当前是否存在cookies：{}", flag);
        if (flag){
            Set<Cookie> cookieSet = cookieMap.get(username);
            WebDriverUtil.addCookies(driver, cookieSet);
            driver.get(BASE_URL);
            waitSeconds(3);
            return true;
        }

        log.info("登录微博，账号为{}", username);
        driver.get(LOGIN_URL);
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
                    return false;
                }
            }

            String curUrl = driver.getCurrentUrl();
            log.info(curUrl);
            if (curUrl.equals(BASE_URL)){
                log.info("网页没有跳转，可能是卡住了，重新输入");
                WebDriverUtil.openNewTab(driver, BASE_URL);
                WebDriverUtil.closeCurrentTab(driver);
                WebDriverUtil.changeWindow(driver);

            } else if (curUrl.contains("security.weibo.com/captcha/geetest")){
                log.info("受到微博限制，进入身份验证");
                waitSeconds(5);
                break;

            } else {
               log.info("成功登陆到m.weibo.cn");
               break;
            }

        }

        cookieMap.put(username, WebDriverUtil.getCookies(driver));
        return true;
    }

    /**
     * 发布微博
     * @param driver
     * @param content
     */
    public static void postWeibo(WebDriver driver, String content){
        waitSeconds(3);
        log.info("发送微博，内容为[{}]", content);
        driver.findElement(By.cssSelector("div.lite-iconf.lite-iconf-releas")).click();
        waitSeconds(1);
        inputContentAndSubmit(driver, content);
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
        WebElement textarea = driver.findElement(By.cssSelector("span.m-wz-def > textarea"));
        textarea.click();
        waitSeconds(2);
        textarea.sendKeys(content);
        //定位发送按钮并点击
        WebElement sendBtn = driver.findElement(By.cssSelector("a.m-send-btn"));
        sendBtn.click();
        waitSeconds(1);
    }
    /**
     * 点赞对应微博
     * @param driver
     * @param index
     */
    public static void likeWeibo(WebDriver driver, int index){
        waitSeconds(3);
        log.info("点赞第{}条微博", index);
        WebElement weibo = getFocusWeibo(driver, index);
        WebElement likeBtn = weibo.findElement(By.cssSelector("i.lite-iconf.lite-iconf-like"));
        likeBtn.click();

    }


    public static boolean subscribeWeiboByInterest(WebDriver driver, List<String> interestList){
        //TODO:随便加个需求，只关注粉丝不低于1万的博主
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
            WebElement preSearchBar =  WebDriverUtil.isElementExist(By.cssSelector("label.m-search"), driver);
            preSearchBar.click();
            waitSeconds(2);

            for (String interest : interestList) {
                //再点击一次搜索框，并输入相关爱好，并回车
                WebElement searchBar = WebDriverUtil.isElementExist(By.cssSelector("input[type = 'search']"), driver);
//
                searchBar.clear();
                searchBar.click();
                searchBar.sendKeys(interest);
                searchBar.sendKeys(Keys.ENTER);
                waitSeconds(2);

                //点击用户标签页，等待跳转
                WebElement userTab = WebDriverUtil.isElementExist(By.cssSelector("ul.nav-item.center > li:nth-child(2)"), driver);
                userTab.click();
                waitSeconds(2);

                //获取到可关注按钮（直接获取到当前所有可关注的按钮的list）
                List<WebElement> followList =  WebDriverUtil.isElementsExist(By.cssSelector("i.m-font.m-font-follow"), driver);
                String jsClick = "arguments[0].click();";
                for(int i = 0; i < followList.size(); i++){
                    if ((i + 1) % 5 == 0){
                        WebDriverUtil.scrollWeibo(driver, 550);
                    }
                    WebDriverUtil.jsExecuter(driver, jsClick, followList.get(i));
//                    cardList.get(i).click();
                    waitSeconds(1);
                }
            }

            return true;

        } catch (NullPointerException npe){
            log.error("没有获取到对应的元素");
            npe.printStackTrace();
            return false;

        } catch (StaleElementReferenceException e){
            e.printStackTrace();
            return false;

        } finally {
            WebDriverUtil.changeWindow(driver);
        }


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
//                WebDriverUtil.closeCurrentTab(driver);
                WebDriverUtil.changeWindow(driver);
                break;
            }
            log.info("出现昵称重复提示框");
            WebElement knowBtn = driver.findElement(By.cssSelector("a.m-btn-text-orange"));
            knowBtn.click();

            //因为昵称冲突，需要修改昵称
            nickName = GenerateInfoUtil.delSuffix(nickName);
            nickName = GenerateInfoUtil.addSuffixToNickName(nickName);
            log.info("重新修改昵称为:{}", nickName);
        }
    }

    public static String setLocation(WebDriver driver,int province){
        initInfoEdit(driver);
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

        return Consts.PROVICE[province] + city;
    }

    public static void setGender(WebDriver driver,int gender){
        initInfoEdit(driver);
        //性别，下拉框
        Select genderSelector = new Select(driver.findElement(By.id("sex")));
        waitSeconds(2);
        genderSelector.selectByIndex(gender);
    }


    public static void setBirthDate(WebDriver driver,LocalDate birthDate){
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
        daySelector.selectByValue(birthDate.getDayOfMonth() + "");
    }

    private static WebElement getFocusWeibo(WebDriver driver, int index){
        return driver.findElements(By.cssSelector("div.wb-item-wrap")).get(index);
    }




}
