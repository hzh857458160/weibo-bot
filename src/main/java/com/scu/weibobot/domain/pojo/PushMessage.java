package com.scu.weibobot.domain.pojo;

import com.scu.weibobot.domain.BotInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * ClassName: PushMessage
 * ClassDesc: 推送消息内容对象
 * Author: HanrAx
 * Date: 2019/03/09
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessage {
    private LocalTime time;
    private BotInfo botInfo;
    private String body;
    private String attach;

    @Override
    public String toString() {
        return "[" + time + "]" + " " + botInfo.getNickName() + "(bot" +
                botInfo.getBotId() + " - " + botInfo.getBotLevel() +
                ") " + body;
    }

    public String toJSON() {
        return "{\"msg\":\"" + toString() + "\", \"attach\":\"" + attach + "\"}";
    }


}
