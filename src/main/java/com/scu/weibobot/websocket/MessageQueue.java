package com.scu.weibobot.websocket;

import com.scu.weibobot.domain.pojo.PushMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ClassName: MessageQueue
 * ClassDesc: 消息队列
 * Author: HanrAx
 * Date: 2019/03/09
 **/
public class MessageQueue {
    //队列大小
    private static final int QUEUE_MAX_SIZE = 10000;
    private static MessageQueue alarmMessageQueue = new MessageQueue();
    //阻塞队列
    private BlockingQueue<PushMessage> blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    private MessageQueue() {
    }

    public static MessageQueue getInstance() {
        return alarmMessageQueue;
    }

    /**
     * 消息入队
     *
     * @param msg
     * @return
     */
    public boolean push(PushMessage msg) {
        return this.blockingQueue.add(msg);//队列满了就抛出异常，不阻塞
    }

    /**
     * 消息出队
     *
     * @return
     */
    public PushMessage poll() {
        PushMessage result = null;
        try {
            result = this.blockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
