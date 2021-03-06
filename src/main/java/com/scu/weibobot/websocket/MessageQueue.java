package com.scu.weibobot.websocket;

import com.scu.weibobot.domain.pojo.PushMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ClassName: MessageQueue
 * ClassDesc: 消息队列
 * Author: HanrAx
 * Date: 2019/03/09
 **/
@Slf4j
public class MessageQueue {
    //队列大小
    private static final int QUEUE_MAX_SIZE = 25;
    private static final int MAP_MAX_SIZE = 100;

    private static Map<Long, BlockingQueue<PushMessage>> map = new ConcurrentHashMap<>(MAP_MAX_SIZE);

    private MessageQueue() {
    }


    /**
     * 消息入队
     *
     * @param msg
     * @return
     */
    public static void push(PushMessage msg, Long botId) {
        log.info("MessageQueue.push({}, {})", msg, botId);
        BlockingQueue<PushMessage> blockingQueue = map.getOrDefault(botId, new LinkedBlockingQueue<>(QUEUE_MAX_SIZE));
        blockingQueue.add(msg);
        if (map.containsKey(botId)) {
            map.replace(botId, blockingQueue);

        } else {
            map.put(botId, blockingQueue);
        }
    }

    /**
     * 消息出队
     *
     * @return
     */
    public static PushMessage poll(Long botId) throws InterruptedException {
        log.info("MessageQueue.poll({})", botId);
        PushMessage result = null;
        if (map.containsKey(botId)) {
            BlockingQueue<PushMessage> blockingQueue = map.get(botId);
            result = blockingQueue.take();
            log.info("poll() return result = {}", result);
            return result;
        } else {
            log.error("MessageQueue.poll() error, no contains key");
            return null;
        }
    }
}
