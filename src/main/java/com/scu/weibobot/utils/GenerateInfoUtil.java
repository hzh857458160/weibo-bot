package com.scu.weibobot.utils;

import com.scu.weibobot.domain.consts.Consts;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;


@Slf4j
public class GenerateInfoUtil {
    private static Random random = new Random();
    private static String nickName = null;
    private static String imgSrc = null;


    //生成昵称
    public static String generateNickName() {
        if (nickName == null){
            getNicknameAndImgSrc();
        }
        String name = addSuffixToNickName(nickName);
        nickName = null;
        return name;

    }
    public static String reAddSuffixToNickName(String tempName){
        if ("".equals(tempName) || tempName == null){
            throw new RuntimeException("当前处于reAddSuffixToNickName(String tempName), 参数错误:" + tempName);
        }
        return addSuffixToNickName(tempName.substring(0, nickName.length() - 1));
    }

    //生成昵称后缀
    public static String addSuffixToNickName(String tempName){
        int ran = random.nextInt(Consts.NICKNAME_SUFFIX.length);
        return tempName + Consts.NICKNAME_SUFFIX[ran];
    }


    /**
     * 生成性别 男0女1
     * @return
     */
    public static int generateGender(){
        return random.nextInt(100) < Consts.BOY_PROB ? 0 : 1;
    }

    /**
     * 生成兴趣爱好，从兴趣库中随机获取三个兴趣
     * @return
     */
    public static String generateInterests(int gender){
        String[] tempArray, a, b;
        if (gender == 0){
            a = Consts.INTERESTS_BOY;
        } else {
            a = Consts.INTERESTS_GIRL;
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
     * 生成头像图片地址
     * @return
     */
    public static String generateImgSrc() throws InterruptedException {
        if (imgSrc == null) {
            getNicknameAndImgSrc();
        }
        String tempImgSrc = imgSrc;
        imgSrc = null;
        return tempImgSrc;


    }

    /**
     * 因为微博选择地址下拉式选项，
     * 所以暂定格式为1-5,
     * 数字为选择地址的位置，
     * 例如:四川 成都 22-1
     */
    public static int generateLocation(){
        return random.nextInt(Consts.PROVINCE.length);
    }

    /**
     * 生成一个随机的日期
     * @return
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
     * 所以需要判断后
     * @param year
     * @param month
     * @return
     */
    private static int generateDay(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year,month - 1, 1);
        int maxDayOfMonth = cal.getActualMaximum(Calendar.DATE);
        return random.nextInt(maxDayOfMonth) + 1;
    }

    /**
     * 根据问卷调查的年龄分布
     * 生成相应概率的年龄
     * 15-19：7%，20-24：40%，25-29：20%，30-39：27%，40以上：6%
     * @return
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
     * N:7% , H:59%, VH:34%
     * @return bot等级
     */
    public static String generateBotLevel(){
        int ran = random.nextInt(100);
        String result;
        if (ran < 7){
            result = Consts.BOT_LEVEL[0];
        } else if (ran < (7 + 59)){
            result = Consts.BOT_LEVEL[1];
        } else {
            result = Consts.BOT_LEVEL[2];
        }
        return result;
    }

    /**
     * 通过在互联网上爬取
     * 来生成相应的昵称与头像地址
     * @return
     */
    private static void getNicknameAndImgSrc() {
        String url = "https://music.163.com/";
        WebDriver driver = null;

        try {
            driver = WebDriverPool.getWebDriver();
            driver.get(url);
            waitSeconds(3);
            //点击歌单选项
            driver.findElement(By.cssSelector("#g_nav2 > div > ul > li:nth-child(3)")).click();
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
            WebElement specHeadImg = commentList.get(ran3).findElement(By.cssSelector("div.head > a > img"));
            WebElement specNickname = commentList.get(ran4).findElement(By.cssSelector("div.cntwrap > div > div > a"));

            //微博h5版本无法设置头像
            imgSrc = specHeadImg.getAttribute("src");
            nickName = specNickname.getText();
            log.info("imgSrc:{}, nickName:{}", imgSrc, nickName);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

    }

    /**
     * 根据机器人当前的等级，已经时间区间，来生成当前使用微博的概率
     *
     * @param botLevel
     * @return
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

        if (isContained(Consts.LOW_ACTIVE_TIME, hour)) {
            log.info("当前处于低活跃度时间", hour);
            prob = botProb * Consts.L_ACTIVE_PROB;

        } else if (isContained(Consts.NORMAL_ACTIVE_TIME, hour)) {
            log.info("当前处于中活跃度时间", hour);
            prob = botProb * Consts.N_ACTIVE_PROB;

        } else if (isContained(Consts.HIGH_ACTIVE_TIME, hour)) {
            log.info("当前处于较高活跃度时间", hour);
            prob = botProb * Consts.H_ACTIVE_PROB;

        } else if (isContained(Consts.VERY_HIGH_ACTIVE_TIME, hour)) {
            log.info("当前处于高活跃度时间", hour);
            prob = botProb * Consts.VH_ACTIVE_PROB;
        } else {
            log.error("hour error");
        }
        return prob;
    }

    private static boolean isContained(int[] array, int x) {
        for (int temp : array) {
            if (temp == x) {
                return true;
            }
        }

        return false;
    }

    public static String generatePostContent(String keyWord) {
        log.info("进入generatePostContent({})", keyWord);
        StringBuilder contentSb = new StringBuilder();
        String url = "https://www.toutiao.com/search/?keyword=" + keyWord;
        WebDriver driver = null;
        try {
            driver = WebDriverPool.getWebDriver();
            log.info("get url：{}", url);
            driver.get(url);
            waitSeconds(3);

            List<WebElement> articleList = WebDriverUtil.forceGetElementList(By.cssSelector("a.img-wrap > img:only-child"), driver);
            log.info("获取到文章列表 size = {}", articleList.size());
            Random random = new Random();
            int ra = random.nextInt(articleList.size());
            log.info("生成随机数为{}", ra);
            articleList.get(ra).click();
            waitSeconds(2);

            WebDriverUtil.changeWindow(driver);
//            List<WebElement> imgList = driver.findElements(By.cssSelector("div.pgc-img > img"));
            List<WebElement> pList = driver.findElements(By.cssSelector("div.article-content > div > p"));

            log.info("获取到文字列表 size = {}", pList.size());
            int i = 0;
            for (WebElement pText : pList) {
                if (i > 1) {
                    break;
                }
                String text = pText.getText().trim();
                log.info("第{}段文字：{}", i, text);
                if ("".equals(text)) {
                    continue;
                }
                contentSb.append(text).append("\n");
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (driver != null) {
                driver.quit();
            }

        }

        return contentSb.toString();
    }


}
