package com.scu.weibobot.utils;

import com.scu.weibobot.domain.Consts;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.*;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;


@Slf4j
public class GenerateInfoUtil {
    private static Random random = new Random();
    private static String nickName = null;
    private static String imgSrc = null;


    //生成昵称
    public static String generateNickName() throws InterruptedException {
        if (nickName == null){
            getNicknameAndImgSrc();
        }
        String name = addSuffixToNickName(nickName);
        nickName = null;
        return name;

    }

    //生成昵称后缀
    public static String addSuffixToNickName(String tempName){
        int ran = random.nextInt(Consts.NICKNAME_SUFFIX.length);
        return tempName + Consts.NICKNAME_SUFFIX[ran];
    }

    public static String delSuffix(String tempName){
        return tempName.substring(0, nickName.length() - 1);
    }

    /**
     * 生成性别 男0女1
     * @return
     */
    public static int generateGender(){
        return random.nextInt(100) < 50 ? 0 : 1;
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
        int ran = random.nextInt(100);
        Random tempRan = new Random();
        int year;
        if (ran < 7){
            year = curYear - 19 + tempRan.nextInt(5);

        } else if (ran < 47){
            year = curYear - 24 + tempRan.nextInt(5);

        } else if (ran < 67){
            year = curYear - 29 + tempRan.nextInt(5);

        } else if (ran < 94){
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
    private static void getNicknameAndImgSrc() throws InterruptedException {
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
//            pageList.get(ran2).click();
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
            if (driver != null){
                WebDriverPool.closeCurrentWebDriver(driver);
            }
        }



    }


}
