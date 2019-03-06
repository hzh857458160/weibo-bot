package com.scu.weibobot.taskexcuter;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.utils.TimeUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Random;


@Slf4j
public class WeiboBotExecutor implements Runnable{

    private BotInfo botInfo;
    private WeiboAccount weiboAccount;

    public void setBotInfo(BotInfo botInfo){
        this.botInfo = botInfo;
    }

    public void setWeiboAccount(WeiboAccount weiboAccount){
        this.weiboAccount = weiboAccount;
    }

    public void run(){
        //判断bot是否为null
        if (botInfo == null){
            log.info("没有设置bot");
            return;
        }
        if (weiboAccount == null){
            log.info("没有设置账号");
            return;
        }
        //获取使用度对应的概率
        double prob = TimeUtil.getProbability(botInfo.getBotLevel());
        log.info("对应使用度概率为{}", prob);
        //随机数
        double nowProb = new Random().nextDouble();
        log.info("当前随机数为{}", nowProb);
        if (nowProb < prob){
            //进入微博，模拟操作
            log.info("进入微博，模拟操作");
            doSthInWeibo(weiboAccount.getUsername(), weiboAccount.getPassword());
        }
    }

    private void doSthInWeibo(String username, String password){
        WeiboOpUtil.loginWeibo(username, password);
        Random random = new Random();
        WeiboOpUtil.likeWeibo(random.nextInt(3));
        WeiboOpUtil.scrollWeibo(500);
        WeiboOpUtil.likeWeibo(random.nextInt(3) + 3);
        WeiboOpUtil.scrollWeibo();
        WeiboOpUtil.reportWeibo(random.nextInt(3) + 6, "");
        WeiboOpUtil.postWeibo("测试，当前时间为" + LocalDateTime.now());
        WeiboOpUtil.closeChrome();
    }

}
