package com.scu.weibobot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.domain.pojo.HttpResult;
import com.scu.weibobot.domain.pojo.NickNameAndImgSrc;
import com.scu.weibobot.taskexecute.WebDriverPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

/**
 * ClassName: GenerateInfoUtil
 * ClassDesc: 工具类，实现对于机器人身份的生成
 * Author: HanrAx
 * Date: 2019/02/15
 **/
@Slf4j
public class GenerateInfoUtil {
    private static Random random = new Random();


    /**
     * 重新为昵称添加后缀
     *
     * @param tempName 原昵称
     * @return 修改后缀的昵称
     */
    public static String reAddSuffixToNickName(String tempName){
        if ("".equals(tempName) || tempName == null){
            throw new RuntimeException("当前处于reAddSuffixToNickName(String tempName), 参数错误:" + tempName);
        }
        return addSuffixToNickName(tempName.substring(0, tempName.length() - 1));
    }

    /**
     * 为昵称生成随机后缀
     *
     * @param tempName 原昵称
     * @return 添加后缀的昵称
     */
    public static String addSuffixToNickName(String tempName) {
        int ran = random.nextInt(Consts.NICKNAME_SUFFIX.length);
        return tempName + Consts.NICKNAME_SUFFIX[ran];
    }


    /**
     * 根据性别分布生成性别 男0女1
     * @return 性别
     */
    public static int generateGender() {
        return random.nextInt(100) + 1 < Consts.BOY_PROB ? 0 : 1;
    }

    /**
     * 根据性别生成兴趣爱好，从兴趣库中随机获取三个兴趣
     * 以兴趣1#兴趣2#兴趣3#....格式拼接
     * @param gender 性别
     * @return 兴趣爱好字符串
     */
    public static String generateInterests(int gender){
        String[] tempArray, a, b;
        if (gender == 0){
            a = Consts.INTERESTS_BOY;
        } else if (gender == 1){
            a = Consts.INTERESTS_GIRL;
        } else {
            throw new RuntimeException("gender参数错误: " + gender);
        }
        b = Consts.INTERESTS_NEUTRAL;
        tempArray = new String[a.length + b.length];
        System.arraycopy(a, 0, tempArray, 0, a.length);
        System.arraycopy(b, 0, tempArray, a.length, b.length);

        Set<Integer> set = new HashSet<>();
        while(set.size() < 3){
            set.add(random.nextInt(tempArray.length));
        }
        StringBuilder interestSb = new StringBuilder();
        for (int tempInt : set){
            interestSb.append(tempArray[tempInt]).append("#");
        }
        return interestSb.toString();
    }


    /**
     * 生成省份库索引
     * @return 省份索引
     */
    public static int generateProvince(){
        return random.nextInt(Consts.PROVINCE.length);
    }

    /**
     * 生成一个随机的出生日期
     * @return 出生日期
     */
    public static LocalDate generateBirthDate(){
        int year = generateBirthYear();
        int month = random.nextInt(12) + 1;
        int day = generateDay(year, month);
        return LocalDate.of(year, month, day);
    }

    /**
     * 生成一个随机的天数
     * 因为每个月的总天数不同
     * 所以需要判断后再随机
     * @param year 年份
     * @param month 月份
     * @return 天数
     */
    private static int generateDay(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year,month - 1, 1);
        int maxDayOfMonth = cal.getActualMaximum(Calendar.DATE);
        return random.nextInt(maxDayOfMonth) + 1;
    }

    /**
     * 根据问卷调查的年龄分布
     * 生成相应概率的年份
     * @return 出生年份
     */
    private static int generateBirthYear(){
        int curYear = LocalDate.now().getYear();
        int ran = random.nextInt(100) + 1; //1 - 100
        Random tempRan = new Random();
        int year;

        if (ran <= Consts.AGE_LESS_THAN_15) {
            year = curYear - 15 + tempRan.nextInt(3);

        } else if (ran <= Consts.AGE_BETWEEN_15_AND_19) {
            year = curYear - 19 + tempRan.nextInt(5);

        } else if (ran < Consts.AGE_BETWEEN_20_AND_24) {
            year = curYear - 24 + tempRan.nextInt(5);

        } else if (ran < Consts.AGE_BETWEEN_25_AND_29) {
            year = curYear - 29 + tempRan.nextInt(5);

        } else if (ran < Consts.AGE_BETWEEN_30_AND_39) {
            year = curYear - 39 + tempRan.nextInt(10);

        } else {
            year = curYear - 50 + tempRan.nextInt(10);
        }
        return year;
    }

    /**
     * 根据问卷调查结果，生成机器人等级
     * @return bot等级
     */
    public static String generateBotLevel() {
        int ran = random.nextInt(100) + 1;
        String result;
        if (ran < Consts.N_LEVEL){
            result = Consts.BOT_LEVEL[0];

        } else if (ran < Consts.H_LEVEL){
            result = Consts.BOT_LEVEL[1];

        } else {
            result = Consts.BOT_LEVEL[2];
        }
        return result;
    }

    /**
     * 通过在互联网上爬取
     * 来生成相应的昵称与头像地址
     */
    public static NickNameAndImgSrc generateNicknameAndImgSrc() {
        String url = "https://music.163.com/";
        WebDriver driver = null;

        try {
            driver = WebDriverPool.getWebDriver();
            driver.get(url);
            waitSeconds(3);
            WebElement songListBtn = WebDriverUtil.waitUntilElementExist(driver, 4, By.cssSelector(
                    "#g_nav2 > div > ul > li:nth-child(3)"));

            //点击歌单选项
            songListBtn.click();
            log.info("点击首页的歌单选项");
            waitSeconds(2);
            //切换到frame中
            driver.switchTo().frame("g_iframe");
            //随机选择一个页码（1~8）
            WebDriverUtil.scrollToBottom(driver);
            Random random = new Random();
            int ran = random.nextInt(8) + 1;
            driver.findElement(By.cssSelector("#m-pl-pager > div > a:nth-child("+ (ran + 1) +")")).click();
            log.info("选择第{}页歌单", ran);
            waitSeconds(2);
            //随机点进一个歌单
            int ran1 = random.nextInt(35) + 1;
            driver.findElement(By.cssSelector("#cateListBox + ul > li:nth-child("+ ran1 +")")).click();
            log.info("选择该页第{}个歌单", ran1);
            waitSeconds(2);

            //随机选择一个评论页码（1~8）
            WebDriverUtil.scrollToBottom(driver);
            List<WebElement> pageList =  WebDriverUtil.isElementsExist(By.cssSelector("a.zpgi:not([style])"), driver);
            if (pageList != null){
                int ran2 = random.nextInt(pageList.size());
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", pageList.get(ran2));
                log.info("选择歌单第{}页评论", ran2);
                waitSeconds(2);
            } else {
                log.info("只有一页评论，没有页码");
            }

            //随机选择一个评论(1~20)
            List<WebElement> commentList = driver.findElements(By.cssSelector("div.itm[data-id]"));
            log.info("该歌单总共有{}个评论", commentList.size());
            if (commentList.size() <= 0){
                log.error("评论数量错误");
            }
            int ran3 = random.nextInt(commentList.size());
            int ran4 = random.nextInt(commentList.size());
            log.info("获取到第{}个和第{}个评论", ran3, ran4);
            WebElement specNickname = commentList.get(ran4).findElement(By.cssSelector("div.cntwrap > div > div > a"));
            String nickName = specNickname.getText();

            commentList.get(ran3).findElement(By.cssSelector("div.head > a > img")).click();
            waitSeconds(2);
            WebElement specHeadImg = WebDriverUtil.forceGetElement(By.cssSelector("#ava > img"), driver);
            String imgSrc = specHeadImg.getAttribute("src");

            log.info("imgSrc:{}, nickName:{}", imgSrc, nickName);
            return new NickNameAndImgSrc(nickName, imgSrc);
        } finally {
            WebDriverPool.closeWebDriver(driver);
        }

    }

    /**
     * 根据机器人当前的等级，已经时间区间，来生成当前使用微博的概率
     *
     * @param botLevel 机器人等级
     * @return 运行概率
     */
    public static double getUseWeiboProb(String botLevel) {
        if (botLevel == null || "".equals(botLevel)) {
            log.error("参数传递出错 [botLevel = {}]", botLevel);
            throw new RuntimeException("参数传递出错[botLevel = " + botLevel + "]");
        }
        double botProb = 0.0;
        for (int i = 0; i < Consts.BOT_LEVEL.length; i++) {
            String temp = Consts.BOT_LEVEL[i];
            if (temp.equals(botLevel)) {
                botProb = Consts.BOT_LEVEL_PROB[i];
            }
        }
        if (botProb == 0.0) {
            log.error("获取系数出错 [botLevel = {}]", botLevel);
            throw new RuntimeException("获取系数出错[botLevel = " + botLevel + "]");
        }
        double prob = 0.0;
        int hour = LocalTime.now().getHour();

        if (isContained(Consts.VERY_LOW_ACTIVE_TIME, hour)) {
            log.info("当前时间为 {} ，处于极低活跃度时间", hour);
            prob = botProb * Consts.VL_ACTIVE_PROB;

        } else if (isContained(Consts.LOW_ACTIVE_TIME, hour)) {
            log.info("当前时间为 {} ，当前处于低活跃度时间", hour);
            prob = botProb * Consts.L_ACTIVE_PROB;

        } else if (isContained(Consts.NORMAL_ACTIVE_TIME, hour)) {
            log.info("当前时间为 {} ，当前处于中活跃度时间", hour);
            prob = botProb * Consts.N_ACTIVE_PROB;

        } else if (isContained(Consts.HIGH_ACTIVE_TIME, hour)) {
            log.info("当前时间为 {} ，当前处于较高活跃度时间", hour);
            prob = botProb * Consts.H_ACTIVE_PROB;

        } else if (isContained(Consts.VERY_HIGH_ACTIVE_TIME, hour)) {
            log.info("当前时间为 {} ，当前处于高活跃度时间", hour);
            prob = botProb * Consts.VH_ACTIVE_PROB;
        } else {
            log.error("hour error");
        }
        return prob;
    }

    /**
     *  数组是否包含某数字
     * @param array 数组
     * @param x 该数字
     * @return 是否包含某数字
     */
    private static boolean isContained(int[] array, int x) {
        for (int temp : array) {
            if (temp == x) {
                return true;
            }
        }
        return false;
    }

    /**
     *  通过爬取网站中的相关文字来生成兴趣微博内容
     * @param keyWord 关键字
     * @return 内容
     */
    public static String generatePostContent(WebDriver driver, String keyWord) {
        log.info("进入generatePostContent({})", keyWord);
        String content = getContentByYiDian(driver, keyWord);
        if (content == null) {
            content = getContentByBaiDu(driver, keyWord);
        }
        return content;
    }

    private static String getContentByYiDian(WebDriver driver, String keyWord) {
        String url = "http://www.yidianzixun.com/";
        String result = "";
        String prePageHandle = driver.getWindowHandle();
        try {
            String mainPageHandle = WebDriverUtil.openNewTab(driver, url);
            WebDriverUtil.changeWindowTo(driver, mainPageHandle);
            WebElement searchBar = new WebDriverWait(driver, 5).until(ExpectedConditions
                    .presenceOfElementLocated(By.cssSelector("div.suggestion-search-box > input")));
            searchBar.click();
            searchBar.sendKeys(keyWord);
            waitSeconds(2);
            WebDriverUtil.forceGetElement(By.cssSelector("div.header-search > div > button"), driver).click();
            waitSeconds(4);

            WebDriverUtil.scrollToBottom(driver);
            waitSeconds(2);
            WebDriverUtil.scrollToBottom(driver);
            waitSeconds(2);
            List<WebElement> newsList = driver.findElements(By
                    .cssSelector("div.channel-news.channel-news-0 > a[data-ctype=\"news\"]"));
            int ra = new Random().nextInt(newsList.size());
            newsList.get(ra).click();
            WebDriverUtil.changeWindow(driver, prePageHandle, mainPageHandle);
            new WebDriverWait(driver, 5).until(ExpectedConditions
                    .presenceOfElementLocated(By.cssSelector("p")));

            List<WebElement> pElementList;
            String articleUrlToken = "www.yidianzixun.com/article";
            if (!driver.getCurrentUrl().contains(articleUrlToken)) {
                pElementList = driver.findElements(By.cssSelector("p"));
            } else {
                pElementList = driver.findElements(By.cssSelector("#imedia-article > p"));
            }
            for (int i = 0; i < 10; i++) {
                String text = pElementList.get(i).getText().trim();
                if (!"".equals(text) && text.length() < 50) {
                    result = text;
                    break;
                }
            }
            WebDriverUtil.changeWindowAndCloseOthers(driver, prePageHandle);
            return result;

        } catch (Exception e) {
            WebDriverUtil.changeWindowAndCloseOthers(driver, prePageHandle);
            return null;
        }
    }

    private static String getContentByBaiDu(WebDriver driver, String keyWord) {
        String url = "https://www.baidu.com/";
        String result = null;
        String prePageHandle = driver.getWindowHandle();
        try {
            String mainPageHandle = WebDriverUtil.openNewTab(driver, url);
            WebDriverUtil.changeWindowTo(driver, mainPageHandle);
            WebElement searchBar = WebDriverUtil.waitUntilElementExist(driver, 5,
                    By.cssSelector("#kw"));
            searchBar.click();
            searchBar.sendKeys(keyWord);
            WebDriverUtil.forceGetElement(By.cssSelector("#su"), driver).click();

            WebElement newsBtn = WebDriverUtil.waitUntilElementExist(driver, 5,
                    By.cssSelector("#s_tab > div > a:nth-child(2)"));
            newsBtn.click();

            WebDriverUtil.waitUntilElementExist(driver, 5,
                    By.cssSelector("#content_left"));

            List<WebElement> titleList = driver.findElements(By.cssSelector("h3.c-title"));
            String href = "";
            while (!href.contains("baijiahao.baidu.com")) {
                int ra = new Random().nextInt(titleList.size());
                WebElement element = titleList.get(ra);
                href = element.findElement(By.cssSelector("a")).getAttribute("href");
            }
            String articlePageHandle = WebDriverUtil.openNewTab(driver, href);
            WebDriverUtil.changeWindowTo(driver, articlePageHandle);
            WebDriverUtil.waitUntilElementExist(driver, 5,
                    By.cssSelector("div.article-content"));
            List<WebElement> spanList = WebDriverUtil.forceGetElementList(By.cssSelector("div.article-content > p"), driver);

            for (int i = 0; i < spanList.size(); i++) {
                String text = spanList.get(i).getText().trim();
                if (!"".equals(text) && text.length() < 50) {
                    result = text;
                    break;
                }
            }
            WebDriverUtil.changeWindowAndCloseOthers(driver, prePageHandle);
            return result;

        } catch (Exception e) {
            WebDriverUtil.changeWindowAndCloseOthers(driver, prePageHandle);
            return null;
        }
    }

    public static String generateCommentByTencentAI(String weiboContent) {
        String url = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_textchat";
        String result = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("app_id", "2116338461");
            map.put("session", "TencentAI");
            map.put("question", weiboContent);
            map.put("time_stamp", Instant.now().getEpochSecond() + "");
            map.put("nonce_str", Instant.now().getEpochSecond() + "");
            HttpResult httpResult = HttpUtil.doGet(url, getSignature(map, "VpmFEpXt2jxGPkCI"));
            JSONObject jsonObject = JSON.parseObject(httpResult.getContent());
            result = jsonObject.getJSONObject("data").getString("answer");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private static Map<String, String> getSignature(Map<String, String> params, String appKey) throws IOException {
        Map<String, String> sortedParams = new TreeMap<>(params);
        Set<Map.Entry<String, String>> entrySet = sortedParams.entrySet();

        StringBuilder baseString = new StringBuilder();
        for (Map.Entry<String, String> param : entrySet) {
            if (param.getValue() != null && !"".equals(param.getKey().trim()) &&
                    !"sign".equals(param.getKey().trim()) && !"".equals(param.getValue())) {

                baseString.append(param.getKey().trim()).append("=")
                        .append(URLEncoder.encode(param.getValue(), "UTF-8")).append("&");
            }
        }
        if (baseString.length() > 0) {
            baseString.deleteCharAt(baseString.length() - 1).append("&app_key=")
                    .append(appKey);
        }
        try {
            String sign = DigestUtils.md5Hex(baseString.toString().getBytes());
            sortedParams.put("sign", sign.toUpperCase());

        } catch (Exception ex) {
            throw new IOException(ex);
        }
        return sortedParams;
    }


}
