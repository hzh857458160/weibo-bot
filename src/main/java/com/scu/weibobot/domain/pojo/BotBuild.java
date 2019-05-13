package com.scu.weibobot.domain.pojo;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ClassName:BotBuild
 * ClassDesc: TODO
 * Author: HanrAx
 * Date: 2019/05/12
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BotBuild {
    private int locationNum;

    private WeiboAccount account;

    private BotInfo info;
}
