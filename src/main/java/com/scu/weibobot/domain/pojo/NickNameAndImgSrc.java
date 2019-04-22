package com.scu.weibobot.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName:NickNameAndImgSrc
 * ClassDesc: pojo类，用于承载爬取到的昵称与头像地址
 * Author: HanrAx
 * Date: 2019/02/16
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NickNameAndImgSrc {
    private String nickName;

    private String imgSrc;

}
