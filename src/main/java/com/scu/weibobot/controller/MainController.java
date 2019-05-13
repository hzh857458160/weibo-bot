package com.scu.weibobot.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.pojo.BotBuild;
import com.scu.weibobot.domain.pojo.NickNameAndImgSrc;
import com.scu.weibobot.domain.pojo.PushMessage;
import com.scu.weibobot.domain.pojo.WeiboUser;
import com.scu.weibobot.service.BotInfoService;
import com.scu.weibobot.service.RedisService;
import com.scu.weibobot.service.WeiboAccountService;
import com.scu.weibobot.taskexecute.WebDriverPool;
import com.scu.weibobot.utils.GenerateInfoUtil;
import com.scu.weibobot.utils.HttpUtil;
import com.scu.weibobot.utils.WebDriverUtil;
import com.scu.weibobot.utils.WeiboOpUtil;
import com.scu.weibobot.websocket.MessageQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.filters.WebdavFixFilter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    @Autowired
    private RedisService redisService;

    private static final String SUBSCRIBE_LIST_KEY = "SubscribeList";
    private static final String HEAD_IMG_SAVE_PATH = "D:\\idea-project\\weibo-bot\\src\\main\\resources\\static\\img\\headimg\\";
    private static Map<String, BotBuild> map = new ConcurrentHashMap<>();



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
        JSONObject result = new JSONObject();
        try {
            //接收post提交的账号与密码
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            int locationNum = 0;
            while (driver == null) {
                locationNum = GenerateInfoUtil.generateProvince();
                driver = WebDriverPool.getWebDriver(Consts.PROVINCE[locationNum]);
                Thread.sleep(1000);
            }
            //验证账号是否能够登陆微博
            if (!WeiboOpUtil.loginWeibo(driver, username, password)) {
                log.warn("账号密码有误，请确认后重试");
                result.put("code", "10");
                result.put("msg", "账号密码有误，请确认后重试");
                throw new RuntimeException(result.getString("msg"));
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
            WeiboAccount account1 = accountService.findByUsername(username);
            //为其生成一个机器人身份（即为账号设置信息），并与该账号绑定。
            BotInfo botInfo = new BotInfo();
            botInfo.setAccountId(account1.getAccountId());
            botInfo.setBirthDate(birthDate);
            botInfo.setBotLevel(GenerateInfoUtil.generateBotLevel());
            botInfo.setGender(gender);
            botInfo.setImgSrc(imgSrc);
            botInfo.setInterests(interests);
            botInfo.setLocation(location);
            botInfo.setNickName(nickName);
            botInfo.setStatus(1);

            //再将资料存入数据库
            botInfoService.addBotInfo(botInfo);
            //初始化关注列表
            Long botId = botInfoService.findBotInfoByAccountId(account1.getAccountId()).getBotId();
            List<String> list = new ArrayList<>(Arrays.asList(interests.split("#")));
            List<WeiboUser> weiboUserList = WeiboOpUtil.subscribeWeiboByInterest(driver, list);
            redisService.hSet(SUBSCRIBE_LIST_KEY, botId + "", JSON.toJSONString(weiboUserList));
            result.put("code", "0");
            result.put("msg", "成功添加账号");
            result.put("attach", botId + "");

        } catch (RuntimeException e) {
            e.printStackTrace();
            log.warn(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            result.put("code", "11");
            result.put("msg", "未知错误");

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
            if (botInfo.getStatus() == 0) {
                i++;
            }
        }
        int hour = LocalTime.now().getHour();
        int nowMinute = LocalTime.now().getMinute();
        String minuteStr = Consts.RUN_PER_HOUR_CRON.split(" ")[2];
        if (nowMinute >= Integer.parseInt(minuteStr)) {
            hour++;
        }
        String startTime = hour + ":" + minuteStr;
        model.addAttribute("botList", botList);
        model.addAttribute("activeCount", i);
        model.addAttribute("allCount", botList.size());
        model.addAttribute("startTime", startTime);
        return "home";
    }


    @GetMapping("/account")
    public String getSpecificBotPage(HttpServletRequest request, Model model) {
        String botId = request.getParameter("botId");
        if (botId == null) {
            throw new RuntimeException("参数错误");
        }
        Long id = Long.valueOf(botId);
        Optional<BotInfo> optBotInfo = botInfoService.findBotInfoById(id);
        optBotInfo.ifPresent(botInfo -> model.addAttribute("bot", botInfo));
        return "showBot";
    }


    @GetMapping("/subscribe")
    public void handleSubscribe(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        JSONObject result = new JSONObject();
        try {
            Long botId = Long.valueOf(request.getParameter("botId"));
            String jsonStr = (String) redisService.hGet(SUBSCRIBE_LIST_KEY, botId + "");
            result.put("code", "0");
            result.put("msg", jsonStr);

        } catch (RuntimeException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", "103");
            result.put("msg", "未知错误");

        } finally {
            PrintWriter out = response.getWriter();
            out.print(result);
            out.flush();
            out.close();
        }

    }

    @PostMapping("/pause")
    @ResponseBody
    public void handlePause(HttpServletRequest request) {
        String botId = request.getParameter("botId");
        log.info(botId);
        Long id = Long.valueOf(botId);
        Optional<BotInfo> optBotInfo = botInfoService.findBotInfoById(id);
        if (optBotInfo.isPresent()) {
            BotInfo botInfo = optBotInfo.get();
            if (botInfo.getStatus() != 2) {
                botInfoService.updateStatusByBotId(Long.valueOf(botId), 2);
            } else {
                botInfoService.updateStatusByBotId(Long.valueOf(botId), 1);
            }
        }


    }

    @PostMapping("/test1")
    @ResponseBody
    public void test1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        WebDriver driver = null;
        JSONObject resultJson = null;
        BotBuild botBuild = new BotBuild();
        //接收post提交的账号与密码
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            int locationNum = 0;
            while (driver == null) {
                locationNum = GenerateInfoUtil.generateProvince();
                driver = WebDriverPool.getWebDriver(Consts.PROVINCE[locationNum]);
//                driver = WebDriverPool.getWebDriver();
            }
            //验证账号是否能够登陆微博
            if (!WeiboOpUtil.loginWeibo(driver, username, password)) {
                log.warn("账号密码有误，请确认后重试");
                resultJson = new JSONObject();
                resultJson.put("code", "101");
                resultJson.put("msg", "账号密码有误，请确认后重试");
            } else {
                log.info("验证账号密码成功");
                WeiboAccount account = new WeiboAccount(0L, username, password);

                botBuild.setAccount(account);
                botBuild.setLocationNum(locationNum);
                String key = UUID.randomUUID().toString();
                map.put(key, botBuild);

                JSONObject attachJson = new JSONObject();
                attachJson.put("key", key);

                resultJson = new JSONObject();
                resultJson.put("code", "0");
                resultJson.put("msg", "账号密码验证成功");
                resultJson.put("attach", attachJson);
                log.info(resultJson.toJSONString());
            }


        } catch (Exception e) {
            e.printStackTrace();
            if (resultJson == null) {
                resultJson = new JSONObject();
                resultJson.put("code", "103");
                resultJson.put("msg", e.getMessage());

            }

        } finally {
            WebDriverPool.closeWebDriver(driver);
            PrintWriter out = response.getWriter();
            out.print(resultJson.toJSONString());
            out.flush();
            out.close();
        }


    }

    @PostMapping("/test2")
    @ResponseBody
    public void test2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        JSONObject resultJson = null;
        try {
            String key = request.getParameter("key");
            if (!map.containsKey(key)) {
                log.error("map中没有该key，操作有误");
                resultJson = new JSONObject();
                resultJson.put("code", "101");
                resultJson.put("msg", "map not contain this key:" + key);
                throw new RuntimeException();
            }

            NickNameAndImgSrc nickNameAndImgSrc = GenerateInfoUtil.generateNicknameAndImgSrc();
            String sourceName = nickNameAndImgSrc.getNickName();
            String nickName = GenerateInfoUtil.addSuffixToNickName(sourceName);
            String imgSrc = nickNameAndImgSrc.getImgSrc();
            int gender = GenerateInfoUtil.generateGender();
            LocalDate birthDate = GenerateInfoUtil.generateBirthDate();
            String interests = GenerateInfoUtil.generateInterests(gender);

            BotInfo botInfo = new BotInfo();
            botInfo.setBirthDate(birthDate);
            botInfo.setBotLevel(GenerateInfoUtil.generateBotLevel());
            botInfo.setGender(gender);
            botInfo.setImgSrc(imgSrc);
            botInfo.setInterests(interests);
            botInfo.setNickName(nickName);
            botInfo.setStatus(1);

            BotBuild botBuild = map.get(key);
            botBuild.setInfo(botInfo);
            map.replace(key, botBuild);

            JSONObject attachJson = new JSONObject();
            attachJson.put("botInfo", botInfo);

            resultJson = new JSONObject();
            resultJson.put("code", "0");
            resultJson.put("msg", "生成信息成功");
            resultJson.put("attach", attachJson);

        } catch (Exception e) {
            e.printStackTrace();
            if (resultJson == null) {
                resultJson = new JSONObject();
                resultJson.put("code", "103");
                resultJson.put("msg", e.getMessage());

            }

        } finally {
            PrintWriter out = response.getWriter();
            out.print(resultJson.toJSONString());
            out.flush();
            out.close();
        }

    }

    @PostMapping("/test3")
    @ResponseBody
    public void test3(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        JSONObject resultJson = null;
        WebDriver driver = null;
        try {
            String key = request.getParameter("key");
            if (!map.containsKey(key)) {
                log.error("map中没有该key，操作有误");
                resultJson = new JSONObject();
                resultJson.put("code", "101");
                resultJson.put("msg", "map not contain this key:" + key);
                throw new RuntimeException();
            }
            //初始化信息
            BotBuild botBuild = map.get(key);
            driver = WebDriverPool.getWebDriver(Consts.PROVINCE[botBuild.getLocationNum()]);
//            driver = WebDriverPool.getWebDriver();
            WeiboOpUtil.loginWeibo(driver, botBuild.getAccount());
            //开始登陆微博设置信息
            BotInfo botInfo = botBuild.getInfo();
            WeiboOpUtil.setGender(driver, botInfo.getGender());
            WeiboOpUtil.setBirthDate(driver, botInfo.getBirthDate());
            String location = WeiboOpUtil.setLocation(driver, botBuild.getLocationNum());
            String nickName = WeiboOpUtil.setNickName(driver, botInfo.getNickName());
            WeiboOpUtil.saveUserSetting(driver);
            //对设置信息进行截图
            String screen = WebDriverUtil.getInfoSettingScreenShot(driver);
            String path = HEAD_IMG_SAVE_PATH + botInfo.getNickName() + ".jpg";
            HttpUtil.downloadImage(botInfo.getImgSrc(), path);
            WeiboOpUtil.setHeadImg(driver, path);
            //初始化关注列表
            List<String> list = new ArrayList<>(Arrays.asList(botInfo.getInterests().split("#")));
            List<WeiboUser> weiboUserList = WeiboOpUtil.subscribeWeiboByInterest(driver, list);
            //最后将所有数据存入数据库
            accountService.addWeiboAccount(botBuild.getAccount());
            Long accountId = accountService.findByUsername(botBuild.getAccount().getUsername()).getAccountId();
            botInfo.setNickName(nickName);
            botInfo.setAccountId(accountId);
            botInfo.setLocation(location);
            log.info("{}", botInfo);
            botInfoService.addBotInfo(botInfo);
            Long botId = botInfoService.findBotInfoByAccountId(accountId).getBotId();
            redisService.hSet(SUBSCRIBE_LIST_KEY, botId + "", JSON.toJSONString(weiboUserList));

            map.remove(key);
            JSONObject attachJson = new JSONObject();
            attachJson.put("screen", screen);
            attachJson.put("botId", botId);

            resultJson = new JSONObject();
            resultJson.put("code", "0");
            resultJson.put("msg", "成功设置信息，并存储到数据库。");
            resultJson.put("attach", attachJson);


        } catch (Exception e) {
            e.printStackTrace();
            if (resultJson == null) {
                resultJson = new JSONObject();
                resultJson.put("code", "103");
                resultJson.put("msg", e.getMessage());

            }

        } finally {
            WebDriverPool.closeWebDriver(driver);
            PrintWriter out = response.getWriter();
            out.print(resultJson.toJSONString());
            out.flush();
            out.close();
        }
    }

    @GetMapping("/test4")
    @ResponseBody
    public void test4(HttpServletRequest request) {

    }

}
