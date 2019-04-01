package com.scu.weibobot.taskexcuter;

import com.scu.weibobot.domain.pojo.PushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Component
@ServerEndpoint("/websocket")
@Slf4j
public class WebSocketServer {

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

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
    public void onOpen(Session session) {
        this.session = session;
        log.info("有新连接加入！");
        pushLogger();

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
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

    public void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    private void pushLogger() {
        Runnable runnable = () -> {
            boolean flag = true;
            while (flag) {
                try {
                    PushMessage pushMessage = MessageQueue.getInstance().poll();
                    sendMessage(pushMessage.toString());

                } catch (IOException e) {
                    log.warn(e.getMessage());
                    flag = false;
                }
            }
        };
        webSocketServer.taskExecutor.execute(runnable);
    }


}
