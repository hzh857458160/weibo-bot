package com.scu.weibobot.controller;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.consts.Consts;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.service.WeiboAccountService;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import com.scu.weibobot.utils.GenerateInfoUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
public class AccountController {
    @Autowired
    private WeiboAccountService accountService;
    @Autowired
    private BotInfoService botInfoService;

    @PostMapping("/account")
    public void addNewBotAccount(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebDriver driver = null;
        response.setContentType("application/json;charset=UTF-8");
        String result = "";
        try {
            response.setHeader("Access-Control-Allow-Origin", "*");
            //接收post提交的账号与密码
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            int locationNum = 0;
            while(driver == null) {
                locationNum = GenerateInfoUtil.generateLocation();
                driver = WebDriverPool.getWebDriver(Consts.PROVINCE[locationNum]);
                Thread.sleep(1000);
            }
            //验证账号是否能够登陆微博
            if (!WeiboOpUtil.loginWeibo(driver, username, password)){
                log.warn("账号密码有误，请确认后重试");
                result = "{\"code\":\"10\",\"msg\":\"账号密码有误，请确认后重试\"}";
                return;
            }
            WeiboAccount account = new WeiboAccount(0L, username, password);
            //先登录账号修改资料(需要返回地址)
            String nickName = GenerateInfoUtil.generateNickName();
            int gender = GenerateInfoUtil.generateGender();
            LocalDate birthDate = GenerateInfoUtil.generateBirthDate();
            String interests = GenerateInfoUtil.generateInterests(gender);

            WeiboOpUtil.setNickName(driver, nickName);
            WeiboOpUtil.setGender(driver, gender);
            WeiboOpUtil.setBirthDate(driver, birthDate);
            String location = WeiboOpUtil.setLocation(driver, locationNum);
            WeiboOpUtil.saveUserSetting(driver);
            log.info("nickName:{}, gender:{}, birthDate:{}, location:{} interests:{}", nickName, gender, birthDate, location, interests);

            //存入账号数据库
            accountService.addWeiboAccount(account);
            //为其生成一个机器人身份（即为账号设置信息），并与该账号绑定。
            BotInfo botInfo = new BotInfo();
            botInfo.setAccountId(accountService.findByUsername(username).getAccountId());
            botInfo.setBirthDate(birthDate);
            botInfo.setBotLevel(GenerateInfoUtil.generateBotLevel());
            botInfo.setGender(gender);
            botInfo.setImgSrc(GenerateInfoUtil.generateImgSrc());
            botInfo.setInterests(interests);
            botInfo.setLocation(location);
            botInfo.setNickName(nickName);
            //再将资料存入数据库
            botInfoService.addBotInfo(botInfo);

            List<String> list = new ArrayList<>(Arrays.asList(interests.split("#")));
            WeiboOpUtil.subscribeWeiboByInterest(driver, list);
            result = "{\"code\":\"0\",\"msg\":\"成功添加账号\"}";

        } catch (Exception e){
            e.printStackTrace();
            result = "{\"code\":\"11\",\"msg\":\"" + e.getMessage() +"\"}";

        } finally {
            WebDriverPool.closeWebDriver(driver);
            PrintWriter out = response.getWriter();
            out.print(result);
            out.flush();
            out.close();
        }


    }
}
