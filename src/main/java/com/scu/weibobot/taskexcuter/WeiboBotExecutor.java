package com.scu.weibobot.taskexcuter;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.consts.Consts;
import com.scu.weibobot.domain.pojo.PushMessage;
import com.scu.weibobot.utils.GenerateInfoUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

@Slf4j
public class WeiboBotExecutor implements Runnable {

    private BotInfo botInfo;
    private WeiboAccount weiboAccount;

    public void setBotInfo(BotInfo botInfo) {
        this.botInfo = botInfo;
    }

    public void setWeiboAccount(WeiboAccount weiboAccount) {
        this.weiboAccount = weiboAccount;
    }


    public void run() {
        if (botInfo == null || weiboAccount == null) {
            log.error("WeiboBotExecutor参数有误 [botInfo:{}, weiboAccount:{}]", botInfo, weiboAccount);
            return;
        }
        doSomethingInWeibo();
    }

    private void doSomethingInWeibo() {
        WebDriver driver = null;
        PushMessage pushMsg = new PushMessage();
        pushMsg.setBotInfo(botInfo);
        try {
            for (int i = 0; i < 3; i++) {
                driver = WebDriverPool.getWebDriver(botInfo);
                waitSeconds(2);
                if (driver != null) {
                    break;
                }
            }

            addMessage(pushMsg, "获取到可用的WebDriver");
            WeiboOpUtil.loginWeibo(driver, weiboAccount.getUsername(), weiboAccount.getPassword());
            addMessage(pushMsg, "登陆微博");
            Random random = new Random();
            int postWeiboRandom = random.nextInt(100) + 1;
            if (postWeiboRandom <= Consts.POST_WEIBO_PROB) {
                int interestRandom = random.nextInt(3);
                String interest = botInfo.getInterests().split("#")[interestRandom];
                String content = GenerateInfoUtil.generatePostContent(interest);
                WeiboOpUtil.postWeibo(driver, content, false);
                addMessage(pushMsg, "发送主题为" + interest + "微博：" + content);
                waitSeconds(2);
            }
            List<WebElement> weiboList = WeiboOpUtil.getWeiboList(driver);
            addMessage(pushMsg, "获取到当前的微博列表 size为" + weiboList.size());
            for (int i = 0; i < weiboList.size(); i++) {
                WebElement weibo = weiboList.get(i);
                WeiboOpUtil.scrollToElement(driver, weibo);
                String weiboName = WeiboOpUtil.getWeiboName(weibo);
                addMessage(pushMsg, "阅读 " + weiboName + " 的微博");
                //30%的概率遇到感兴趣的微博，仔细阅读并点赞
                int likeRandom = random.nextInt(100) + 1;
                if (likeRandom <= Consts.LIKE_WEIBO_PROB) {
                    waitSeconds((random.nextDouble() + 2.0));
                    WeiboOpUtil.likeWeibo(weibo);
                    addMessage(pushMsg, "仔细阅读点赞 " + weiboName + " 的微博");
                } else {
                    //普通微博，简单阅读几秒
                    waitSeconds((random.nextDouble() + 0.7));
                }
                //20%的概率评论
                int commentRandom = random.nextInt(100) + 1;
                if (commentRandom <= Consts.COMMENT_WEIBO_PROB) {
//                    WeiboOpUtil.commentWeibo(driver, weibo, "");
//                    weiboList = WeiboOpUtil.getWeiboList(driver);
//                    addMessage(pushMsg, "评论 " + weiboName + " 的微博：" + content);
                }
                waitSeconds(1);
                //20%的概率转发
                int reportRandom = random.nextInt(100) + 1;
                if (reportRandom <= Consts.REPORT_WEIBO_PROB) {
                    WeiboOpUtil.reportWeibo(driver, weibo);
                    weiboList = WeiboOpUtil.getWeiboList(driver);
                    addMessage(pushMsg, "转发 " + weiboName + " 的微博");
                }
                waitSeconds(1);
            }

        } finally {
            WebDriverPool.closeWebDriver(driver);
        }

    }

    private void addMessage(PushMessage pushMsg, String msg) {
        pushMsg.setBody(msg);
        pushMsg.setTime(LocalTime.now());
        MessageQueue.getInstance().push(pushMsg);
    }

}
