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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: WebSocketServer
 * ClassDesc: WebSocket后台控制类
 * Author: HanrAx
 * Date: 2019/03/09
 **/
@Component
@ServerEndpoint("/websocket/{botId}")
@Slf4j
public class WebSocketServer {

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
//    private static Map<Long, Session> map = new ConcurrentHashMap<>();

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    public static WebSocketServer webSocketServer;


    @PostConstruct
    public void init() {
        webSocketServer = this;
        webSocketServer.taskExecutor = this.taskExecutor;
    }


    /**
     * 收到新连接关闭调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("botId") String botId) {
        log.info("新连接加入：[botId:{}]", botId);
        Long id = Long.valueOf(botId);
//        map.put(id, session);
        webSocketServer.taskExecutor.execute(pushLogger(id, session));
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        log.info("有连接关闭！");


    }


    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     **/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自客户端的消息:" + message);

    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误");
        error.printStackTrace();
    }

    /**
     * 通过WebSocket发送消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message, Session session) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    /**
     * 开启线程，不断推送消息到前端
     */
    private Runnable pushLogger(Long botId, Session session) {
        log.info("pushLogger({}, {})", botId, session);
        return () -> {
            boolean flag = true;
            while (flag) {
                try {
                    PushMessage pushMessage = MessageQueue.poll(botId);
                    if (pushMessage == null) {
                        Thread.sleep(2000);
                        continue;
                    }
                    log.info("push logger : {}", pushMessage);
                    sendMessage(pushMessage.toJSON(), session);

                } catch (Exception e) {
                    e.printStackTrace();
                    flag = false;

                }
            }
        };

    }

}
