package com.scu.weibobot.domain.pojo;

import com.scu.weibobot.domain.BotInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessage {
    private LocalTime time;
    private BotInfo botInfo;
    private String body;

    @Override
    public String toString() {
        //bot1-botname(botLevel) time body in threadName
        return "[" + time + "]" + " " + botInfo.getNickName() + "(bot" +
                botInfo.getBotId() + " - " + botInfo.getBotLevel() +
                ") " + body;
    }
}
