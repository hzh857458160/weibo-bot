package com.scu.weibobot.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ClassName:WeiboUser
 * ClassDesc: TODO
 * Author: HanrAx
 * Date: 2019/04/01
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WeiboUser {
    private String headImg;

    private String nickName;

    private String intro;
}
