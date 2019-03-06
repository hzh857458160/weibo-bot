package com.scu.weibobot.controller;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.consts.BotInfoConst;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.service.WeiboAccountService;
import com.scu.weibobot.utils.GenerateInfoUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Slf4j
@Controller
public class AccountController {
    @Autowired
    private WeiboAccountService accountService;
    @Autowired
    private BotInfoService botInfoService;

    @PostMapping("/account")
    public void addNewBotAccount(HttpServletRequest request, HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "*");
        //接收post提交的账号与密码
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        //验证账号是否能够登陆微博
        if (!WeiboOpUtil.loginWeibo(username, password)){
            log.info("账号密码有误，请确认后重试");
            return;
        }
        WeiboAccount account = new WeiboAccount(0L, username, password);
        //先登录账号修改资料(需要返回地址)
//        String nickName = GenerateInfoUtil.generateNickName();
        String nickName = "Holly170616-l";
        int gender = GenerateInfoUtil.generateGender();
        LocalDate birthDate = GenerateInfoUtil.generateBirthDate();
        int location = GenerateInfoUtil.generateLocation();
        log.info("nickName:{}, gender:{}, birthDate:{}, location:{}", nickName, gender, birthDate, location);


//        if (!WeiboOpUtil.setBotInfo(nickName, gender, location, birthDate)){
//            log.info("设置账号资料出错，请确认后重试");
//            return;
//        }

        if (!WeiboOpUtil.setBotInfo(gender, location, birthDate)){
            log.info("设置账号资料出错，请确认后重试");
            return;
        }
        //存入账号数据库
        accountService.addWeiboAccount(account);
        //为其生成一个机器人身份（即为账号设置信息），并与该账号绑定。
        BotInfo botInfo = new BotInfo();
        botInfo.setAccountId(accountService.findByUsername(username).getAccountId());
        botInfo.setBirthDate(birthDate);
        botInfo.setBotLevel(GenerateInfoUtil.generateBotLevel());
        botInfo.setGender(gender);
        botInfo.setImgSrc(GenerateInfoUtil.generateImgSrc());
        botInfo.setInterests(GenerateInfoUtil.generateInterests());
        botInfo.setLocation(BotInfoConst.PROVICE[location]);
        botInfo.setNickName(nickName);
        //再将资料存入数据库
        botInfoService.addBotInfo(botInfo);

    }
}
