package com.scu.weibobot.utils;

import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.domain.pojo.HttpResult;
import com.scu.weibobot.domain.pojo.ProxyIp;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

/**
 * @author HanrAx
 */
@Slf4j
public class ProxyUtil {

    private static final String GET_KDL_PROXY_URL = "http://dps.kdlapi.com/api/getdps/?orderid=995816886251587&signature=7ouze7pkgq2aqei9eob49zqlri8qvfxb&num=1&area=PROVINCE_REPLACE&pt=1&ut=1&sep=1";
    private static final String KDL_WHITE_LIST_URL = "https://dev.kdlapi.com/api/setipwhitelist";
    private static final String KDL_ORDER_ID = "995816886251587";
    private static final String KDL_API_KEY = "7ouze7pkgq2aqei9eob49zqlri8qvfxb";
    private static final String KUAIDAILI = "KUAIDAILI";

    private static final String GET_ZHIMA_FREE_PROXY_URL = "http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=PROVINCE_REPLACE&city=0&yys=0&port=1&pack=49399&ts=0&ys=0&cs=0&lb=6&sb=#&pb=45&mr=3&regions=";
    private static final String GET_ZHIMA_PROXY_URL = "http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=PROVINCE_REPLACE&city=0&yys=0&port=1&time=1&ts=0&ys=0&cs=0&lb=6&sb=#&pb=45&mr=3&regions=";
    private static final String ZHIMA_WHITE_LIST_URL = "http://web.http.cnapi.cc/index/index/save_white?neek=68222&appkey=1f5444badb015126fd0edf9d6818e95d&white=IP_REPLACE";
    private static final String ZHIMA_DEL_WHITE_URL = "http://web.http.cnapi.cc/index/index/del_white?neek=68222&appkey=1f5444badb015126fd0edf9d6818e95d&white=IP_REPLACE";
    private static final String ZHIMA = "ZHIMA";

    private static final String CACHE_IP_KEY = "CACHE_IP:";

    /**
     * 因为学校拨号登陆使用了DHCP，所以每次拨号外网ip都不同
     * 需要把当前ip添加至快代理的白名单
     */
    public static void setKDLWhiteList(String currentIp) {
        try {
            Map<String, String> map = new HashMap<>(3);
            map.put("orderid", KDL_ORDER_ID);
            map.put("iplist", currentIp);
            map.put("signature", KDL_API_KEY);
            HttpResult httpResult = HttpUtil.doPost(KDL_WHITE_LIST_URL, map);
            log.info("KDL 设置白名单结果：{}", httpResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delZHIMAWhiteList(String lastIp) {
        log.info("delZHIMAWhiteList()");
        String delWhiteUrl = ZHIMA_DEL_WHITE_URL.replace("IP_REPLACE", lastIp);
        try {
            HttpResult httpResult = HttpUtil.doGet(delWhiteUrl);
            log.info("删除白名单结果：{}", httpResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setZHIMAWhiteList(String currentIp) {
        log.info("进入setZHIMAWhiteList()");
        String setWhiteUrl = ZHIMA_WHITE_LIST_URL.replace("IP_REPLACE", currentIp);

        try {
            HttpResult httpResult = HttpUtil.doGet(setWhiteUrl);
            System.out.println("设置白名单结果：" + httpResult);
            log.info("设置白名单结果：{}", httpResult);
            WebDriverUtil.cacheCurrentIp(CACHE_IP_KEY, currentIp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized ProxyIp getOneProxyWithProvince(String province) {
        String currentIp = getLocalIp();
        String lastIp = WebDriverUtil.getCachedIp(CACHE_IP_KEY);
        log.info("当前ip:" + currentIp);
        log.info("缓存ip:" + lastIp);
        if (!currentIp.equals(lastIp)) {
            setZHIMAWhiteList(currentIp);
            delZHIMAWhiteList(lastIp);
            setKDLWhiteList(currentIp);
        }
        ProxyIp proxyIp;
        proxyIp = getProxy(province, KUAIDAILI);
        if (proxyIp != null) {
            return proxyIp;
        }
        proxyIp = getProxy(province, ZHIMA);
        if (proxyIp != null) {
            return proxyIp;
        }
        return null;
    }

    private static ProxyIp getProxy(String province, String brand) {
        String url;
        if (KUAIDAILI.equals(brand)) {
            url = GET_KDL_PROXY_URL.replace("PROVINCE_REPLACE", province);

        } else if (ZHIMA.equals(brand)) {
            String provinceId = getProvinceId(province);
            if (provinceId == null) {
                return null;
            }
            url = GET_ZHIMA_PROXY_URL.replace("PROVINCE_REPLACE", provinceId);
        } else {
            throw new RuntimeException("getProxy()参数错误 brand = " + brand);
        }

        try {
            HttpResult httpResult = HttpUtil.doGet(url);
            log.info("{} return: [{}]", brand, httpResult.toString());
            String content = httpResult.getContent();
            if (content.contains("ERROR") || content.contains("\"success\":false")) {
                waitSeconds(1);
                return null;
            }
            ProxyIp proxyIp = new ProxyIp();
            int index = content.indexOf(":");
            if (index == -1) {
                return null;
            }
            String ip = content.substring(0, index);
            String port = content.substring(index + 1);
            proxyIp.setIp(ip);
            proxyIp.setPort(Integer.parseInt(port));
            proxyIp.setLocation(province);
            proxyIp.setAvailable(true);
            log.info("{}: {}", brand, proxyIp);
            return proxyIp;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String getLocalIp() {
        InetAddress ia;
        try {
            ia = InetAddress.getLocalHost();
            String localIp = ia.getHostAddress();
            return localIp;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("获取本机ip失败");
    }

    private static String getProvinceId(String province) {
        int index = -1;
        for (int i = 0; i < Consts.REGIONS_PROVINCE.length; i++) {
            if (Consts.REGIONS_PROVINCE[i].equals(province)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            log.warn("getProvinceId()错误，没有对应的身份 [{}]", province);
            return null;
        }

        return Consts.REGIONS[index] + "";

    }


}
