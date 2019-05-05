package com.scu.weibobot.taskexcuter;

import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.pojo.PushMessage;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.utils.GenerateInfoUtil;
import com.scu.weibobot.utils.WebDriverUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import com.scu.weibobot.websocket.MessageQueue;
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

    public void setBotInfo(BotInfo botInfo) {
        this.botInfo = botInfo;
    }

    public void setWeiboAccount(WeiboAccount weiboAccount) {
        this.weiboAccount = weiboAccount;
    }

    public void setBotInfoService(BotInfoService botInfoService) {
        this.botInfoService = botInfoService;
    }


    public void run() {
        if (botInfo == null || weiboAccount == null || botInfoService == null) {
            log.error("WeiboBotExecutor参数有误 [botInfo:{}, weiboAccount:{}]", botInfo, weiboAccount);
            return;
        }
        doSomethingInWeibo();
    }

    private void doSomethingInWeibo() {
        Long id = botInfo.getBotId();
        String nickName = botInfo.getNickName();
        botInfoService.updateStatusByBotId(id, 0);
        WebDriver driver = null;
        PushMessage pushMsg = new PushMessage();
        pushMsg.setBotInfo(botInfo);
        try {
            driver = WebDriverPool.getWebDriver(botInfo);
            if (driver == null) {
                driver = WebDriverPool.getWebDriver();
            }
            log.info("{} 获取到可用的WebDriver", nickName);
            addMessage(pushMsg, "获取到可用的WebDriver");
            WeiboOpUtil.loginWeibo(driver, weiboAccount);
            log.info("{} 登录微博", nickName);
            addMessage(pushMsg, "登陆微博");
            Random random = new Random();
            int postWeiboRandom = random.nextInt(100) + 1;
            if (postWeiboRandom <= Consts.POST_WEIBO_PROB) {
                int interestRandom = random.nextInt(3);
                //随机获取兴趣
                String interest = botInfo.getInterests().split("#")[interestRandom];
                log.info("{} 获取生成内容", nickName);
                //生成发送内容并发送微博
                String content = GenerateInfoUtil.generatePostContent(interest);
                log.info("{} 发送微博", nickName);
                WeiboOpUtil.postWeibo(driver, content, false);
                //获取刚发送的微博
                WebElement recentWeibo = WeiboOpUtil.getRecentPostWeibo(driver);
                //生成截图，并返回主页
                String imgName = WebDriverUtil.getScreenShotFileName(recentWeibo);
                WeiboOpUtil.backToMainPage(driver);

                addMessage(pushMsg, "发送" + interest + "相关的微博：" + content, imgName);
                waitSeconds(2);
            }
            String findWeibo = "div.wb-item-wrap:nth-child(INDEX)";
            log.info("{} 开始阅读微博", nickName);
            addMessage(pushMsg, "开始阅读微博");
            for (int i = 1; i <= 20; i++) {
                WebElement weibo = WebDriverUtil.forceGetElement(By.cssSelector(
                        findWeibo.replace("INDEX", i + "")), driver);
                WebDriverUtil.scrollToElement(driver, weibo);
                String weiboName = WeiboOpUtil.getWeiboName(weibo);
                log.info("{} 开始阅读{}微博", nickName, weiboName);
                addMessage(pushMsg, "阅读 " + weiboName + " 的微博");
                //30%的概率遇到感兴趣的微博，仔细阅读并点赞
                int likeRandom = random.nextInt(100) + 1;
                if (likeRandom <= Consts.LIKE_WEIBO_PROB) {
                    waitSeconds((random.nextDouble() + 2.0));
                    WeiboOpUtil.likeWeibo(weibo);
                    String imgName = WebDriverUtil.getScreenShotFileName(weibo);
                    log.info("{} 点赞微博并截图", nickName);
                    addMessage(pushMsg, "仔细阅读点赞 " + weiboName + " 的微博", imgName);
                } else {
                    //普通微博，简单阅读几秒
                    waitSeconds((random.nextDouble() + 0.7));
                }
                //20%的概率评论
//                int commentRandom = random.nextInt(100) + 1;
//                if (commentRandom <= Consts.COMMENT_WEIBO_PROB) {
//                    WeiboOpUtil.commentWeibo(driver, weibo, "");
//                    addMessage(pushMsg, "评论 " + weiboName + " 的微博：" + content);
//                }
                waitSeconds(1);
                //20%的概率转发
                int reportRandom = random.nextInt(100) + 1;
                if (reportRandom <= Consts.REPORT_WEIBO_PROB) {
                    WeiboOpUtil.reportWeibo(driver, weibo);
                    //获取刚发送的微博
                    WebElement recentWeibo = WeiboOpUtil.getRecentPostWeibo(driver);
                    String imgPath = WebDriverUtil.getScreenShotFileName(recentWeibo);
                    WeiboOpUtil.backToMainPage(driver);
                    log.info("{} 转发{}的微博", nickName, weiboName);
                    addMessage(pushMsg, "转发 " + weiboName + " 的微博", imgPath);
                }
                waitSeconds(1);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            WebDriverPool.closeWebDriver(driver);
            botInfoService.updateStatusByBotId(botInfo.getBotId(), 1);
        }

    }

    private void addMessage(PushMessage pushMsg, String msg, String attach) {
        Long botId = pushMsg.getBotInfo().getBotId();
        pushMsg.setBody(msg);
        pushMsg.setTime(LocalTime.now());
        pushMsg.setAttach(attach);
        MessageQueue.getInstance().push(pushMsg, botId);
    }

    private void addMessage(PushMessage pushMsg, String msg) {
        Long botId = pushMsg.getBotInfo().getBotId();
        pushMsg.setBody(msg);
        pushMsg.setTime(LocalTime.now());
        MessageQueue.getInstance().push(pushMsg, botId);
    }

}
