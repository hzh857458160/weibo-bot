package com.scu.weibobot.controller;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.domain.pojo.NickNameAndImgSrc;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.service.WeiboAccountService;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import com.scu.weibobot.utils.GenerateInfoUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * ClassName: MainController
 * ClassDesc: 主要处理请求的Controller
 * Author: HanrAx
 * Date: 2019/02/14
 **/
@Slf4j
@Controller
public class MainController {
    @Autowired
    private WeiboAccountService accountService;
    @Autowired
    private BotInfoService botInfoService;

    /**
     * 添加新机器人账号接口
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @ResponseBody
    @PostMapping("/account")
    public void addNewBotAccount(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebDriver driver = null;
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        String result = "";
        try {

            //接收post提交的账号与密码
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            int locationNum = 0;
            while(driver == null) {
                locationNum = GenerateInfoUtil.generateProvince();
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
            NickNameAndImgSrc nickNameAndImgSrc = GenerateInfoUtil.generateNicknameAndImgSrc();
            String nickName = nickNameAndImgSrc.getNickName();
            String imgSrc = nickNameAndImgSrc.getImgSrc();
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
            botInfo.setImgSrc(imgSrc);
            botInfo.setInterests(interests);
            botInfo.setLocation(location);
            botInfo.setNickName(nickName);
            botInfo.setEnable(true);

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


    @GetMapping("/index")
    public String index(Model model) {
        List<BotInfo> botList = botInfoService.finaAll();
        int i = 0;
        for (BotInfo botInfo : botList) {
            if (botInfo.isEnable()) {
                i++;
            }
        }

        model.addAttribute("botList", botList);
        model.addAttribute("activeCount", i);
        return "home";
    }


    @GetMapping("/account")
    public String getSpecificBotPage(HttpServletRequest request, Model model) {
        String botId = request.getParameter("botId");
        Optional<BotInfo> optBotInfo = botInfoService.findBotInfoById(Long.valueOf(botId));
        optBotInfo.ifPresent(botInfo -> model.addAttribute("bot", botInfo));
        return "showBot";
    }
}
