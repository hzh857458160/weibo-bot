package com.scu.weibobot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BotInfo {
    //机器人的编号
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long botId;

    //当前机器人对应的账号
    private Long accountId;

    //微博昵称
    private String nickName;

    //真实名称，可能会用到
    //private String trueName;

    //性别 男0女1
    private int gender;

    //兴趣爱好 格式暂定为 #爱好1#爱好2#爱好3#.....
    private String interests;

    //头像图片地址
    private String imgSrc;

    //所在地
    private String location;

    //出生日期
    private LocalDate birthDate;

    //预设机器人使用微博程度 N(normal)、H(high)、VH(very high)
    private String botLevel;

    //需求中可以暂停机器人发言，使用该字段来判断，true为可以，false为不可以
    private boolean enable;


}
