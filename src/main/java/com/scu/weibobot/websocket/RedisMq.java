package com.scu.weibobot.websocket;

import com.scu.weibobot.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * ClassName:RedisMq
 * ClassDesc: TODO
 * Author: HanrAx
 * Date: 2019/05/19
 **/
@Component
@Slf4j
@SuppressWarnings("unchecked")
public class RedisMq {

    public static RedisMq redisMq;

    @Autowired
    private RedisService redisService;

    private final String KEY_PREFIX = "REDIS_MQ_";

    @PostConstruct
    public void init() {
        redisMq = this;
        redisMq.redisService = this.redisService;
    }

    public void clearList(String key) {
        redisMq.redisService.del(KEY_PREFIX + key);
    }

    /**
     * 发送消息
     *
     * @param message
     */
    public void push(String key, String message) {
        log.info("push {}: {}", key, message);
        redisMq.redisService.lPush(KEY_PREFIX + key, message);
    }

    /**
     * 获取消息,可以对消息进行监听，没有超过监听事件，则返回消息为null
     * rightPop：1.key,2.超时时间，3.超时时间类型
     *
     * @return
     */
    public String pop(String key) {
        String result = redisMq.redisService.lPop(KEY_PREFIX + key);
        if (result != null) {
            log.info("pop {}:{}", key, result);
        }
        return result;
    }

}
