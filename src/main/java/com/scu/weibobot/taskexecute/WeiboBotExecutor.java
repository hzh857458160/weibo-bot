package com.scu.weibobot.taskexecute;

import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.pojo.PushMessage;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.utils.GenerateInfoUtil;
import com.scu.weibobot.utils.WebDriverUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import com.scu.weibobot.websocket.RedisMq;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Random;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

/**
 * ClassName: WeiboBotExecutor
 * ClassDesc: 实现Runnable接口，机器人线程类
 * Author: HanrAx
 * Date: 2019/03/09
 **/
@Slf4j
public class WeiboBotExecutor implements Runnable {

    private BotInfo botInfo;
    private WeiboAccount weiboAccount;
    private BotInfoService botInfoService;
    private Random random = new Random();
    private String nickName;
    private RedisMq redisMq = new RedisMq();

    public void setBotInfo(BotInfo botInfo) {
        this.botInfo = botInfo;
        this.nickName = botInfo.getNickName();
    }

    public void setWeiboAccount(WeiboAccount weiboAccount) {
        this.weiboAccount = weiboAccount;
    }

    public void setBotInfoService(BotInfoService botInfoService) {
        this.botInfoService = botInfoService;
    }


    @Override
    public void run() {
        if (botInfo == null || weiboAccount == null || botInfoService == null) {
            log.error("WeiboBotExecutor参数有误 [botInfo:{}, weiboAccount:{}]", botInfo, weiboAccount);
            return;
        }
        doSomethingInWeibo();
    }

    private void doSomethingInWeibo() {
        //修改运行状态
        botInfoService.updateStatusByBotId(botInfo.getBotId(), 0);
        redisMq.clearList(botInfo.getBotId() + "");
        WebDriver driver = null;
        //初始化推送日志
        PushMessage pushMsg = new PushMessage();
        pushMsg.setBotInfo(botInfo);

        try {
            //获取driver
            driver = WebDriverPool.getWebDriver(botInfo);
            if (driver == null) {
                driver = WebDriverPool.getWebDriver();
            }
            addMessage(pushMsg, "获取到可用的WebDriver");
            //登陆微博
            WeiboOpUtil.loginWeibo(driver, weiboAccount);
            addMessage(pushMsg, "登陆微博");
            //判断是否发布微博
            int postWeiboRandom = random.nextInt(100) + 1;
            if (postWeiboRandom <= Consts.POST_WEIBO_PROB) {
                postWeibo(driver, pushMsg);
            }
            //开始遍历微博列表
            String findWeibo = "div.wb-item-wrap:nth-child(INDEX)";
            addMessage(pushMsg, "开始阅读微博");
            for (int i = 1; i < 20; i++) {
                By getWeiboBy = By.cssSelector(findWeibo.replace("INDEX", i + ""));
                WebElement weibo = WebDriverUtil.forceGetElement(getWeiboBy, driver);
                WebDriverUtil.scrollToElement(driver, weibo);
                String weiboName = WeiboOpUtil.getWeiboName(weibo);

                addMessage(pushMsg, "阅读 " + weiboName + " 的微博");
                //30%的概率遇到感兴趣的微博，仔细阅读并点赞
                if (getRandomNum() <= Consts.LIKE_WEIBO_PROB) {
                    //点赞
                    waitSeconds((random.nextDouble() + 2.0));
                    WeiboOpUtil.likeWeibo(driver, weibo);
                    //截图
                    String imgName = WebDriverUtil.screenShot4Common(driver, weibo);
                    addMessage(pushMsg, "仔细阅读并点赞 " + weiboName + " 的微博", imgName);
                } else {
                    //普通微博，简单阅读几秒
                    waitSeconds((random.nextDouble() + 0.7));
                }
                //20%的概率评论
                if (getRandomNum() <= Consts.COMMENT_WEIBO_PROB) {
                    //获取评论文本并评论
                    String weiboContent = WeiboOpUtil.getWeiboText(weibo);
                    String comment = GenerateInfoUtil.generateCommentByTencentAI(weiboContent);
                    log.info("comment content: {}", comment);
                    if ("".equals(comment)) {
                        log.warn("comment fail, content is blank");
                    } else {
                        WeiboOpUtil.commentWeibo(driver, weibo, comment);
                        //截图并返回
                        String screenshot = WebDriverUtil.screenShot4Comment(driver);
                        addMessage(pushMsg, "评论 " + weiboName + " 的微博：" + comment, screenshot);
                        WeiboOpUtil.backToMainPage(driver);
                        weibo = WebDriverUtil.forceGetElement(getWeiboBy, driver);

                    }
                }
                waitSeconds(1);
                //20%的概率转发
                if (getRandomNum() <= Consts.REPORT_WEIBO_PROB) {
                    WeiboOpUtil.reportWeibo(driver, weibo);
                    //获取刚发送的微博
                    WebElement recentWeibo = WeiboOpUtil.getRecentPostWeibo(driver);
                    String imgPath = WebDriverUtil.screenShot4Common(driver, recentWeibo);
                    WeiboOpUtil.backToMainPage(driver);
                    log.info("{} 转发{}的微博", nickName, weiboName);
                    addMessage(pushMsg, "转发 " + weiboName + " 的微博", imgPath);
                }
                waitSeconds(1);
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.info(driver.getCurrentUrl());

        } finally {
            WebDriverPool.closeWebDriver(driver);
            botInfoService.updateStatusByBotId(botInfo.getBotId(), 1);
        }

    }

    private void postWeibo(WebDriver driver, PushMessage pushMsg) throws IOException {
        int interestRandom = random.nextInt(3);
        //随机获取兴趣
        String interest = botInfo.getInterests().split("#")[interestRandom];
        //生成发送内容并发送微博
        String content = GenerateInfoUtil.generatePostContent(driver, interest);
        log.info("{} 获取生成内容", nickName);

        if (content != null) {
            WeiboOpUtil.postWeibo(driver, content, false);
            log.info("{} 发送微博: {}", nickName, content);
            //获取刚发送的微博
            WebElement recentWeibo = WeiboOpUtil.getRecentPostWeibo(driver);
            //生成截图，并返回主页
            String imgName = WebDriverUtil.screenShot4Common(driver, recentWeibo);
            WeiboOpUtil.backToMainPage(driver);
            addMessage(pushMsg, "发送" + interest + "相关的微博：", imgName);
            waitSeconds(2);
        } else {
            log.warn("没有获取到发送内容");
        }
    }

    private void addMessage(PushMessage pushMsg, String msg, String attach) {
        log.info("{} " + msg, nickName);
        Long botId = pushMsg.getBotInfo().getBotId();
        pushMsg.setBody(msg);
        pushMsg.setTime(LocalTime.now());
        pushMsg.setAttach(attach);
        redisMq.push(botId + "", pushMsg.toJSON());
//        MessageQueue.push(pushMsg, botId);
    }

    private void addMessage(PushMessage pushMsg, String msg) {
        addMessage(pushMsg, msg, null);
    }

    private int getRandomNum() {
        return random.nextInt(100) + 1;
    }
}
