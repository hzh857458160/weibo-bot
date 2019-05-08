package com.scu.weibobot.websocket;

import com.scu.weibobot.domain.pojo.PushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ClassName:WebSocket
 * ClassDesc: TODO
 * Author: HanrAx
 * Date: 2019/05/06
 **/
@ServerEndpoint("/websocket/{botId}")
@Component
@Slf4j
public class WebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    private volatile boolean endFlag = true;

    private Long id;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    public static WebSocket webSocket;


    @PostConstruct
    public void init() {
        webSocket = this;
        webSocket.taskExecutor = this.taskExecutor;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("botId") String botId) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        id = Long.valueOf(botId);
        webSocket.taskExecutor.execute(pushLogger());
        log.info("当前在线人数为" + getOnlineCount());

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
        endFlag = false;
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     **/
    @OnMessage
    public void onMessage(String message) {
        log.info("来自客户端的消息:" + message);


    }


    /**
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 开启线程，不断推送消息到前端
     */
    private Runnable pushLogger() {
        log.info("pushLogger()");
        return () -> {
            while (endFlag) {
                try {
                    PushMessage pushMessage = MessageQueue.getInstance().poll(id);
                    log.info("push logger : {}", pushMessage);
                    sendMessage(pushMessage.toJSON());

                } catch (Exception e) {
                    e.printStackTrace();
                    endFlag = false;

                }
            }
        };

    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }
}


