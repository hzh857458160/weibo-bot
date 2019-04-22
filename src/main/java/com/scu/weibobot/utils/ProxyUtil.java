package com.scu.weibobot.utils;

import com.scu.weibobot.consts.Consts;
import com.scu.weibobot.domain.pojo.ProxyIp;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

@Slf4j
public class ProxyUtil {
    private static String LAST_IP = "";

    private static final String GET_KDL_PROXY_URL = "http://dps.kdlapi.com/api/getdps/?orderid=915341030956045&num=1&area=AREA_REPLACE&pt=1&ut=1&sep=%23";
    private static final String KDL_WHITE_LIST_URL = "https://dev.kdlapi.com/api/setipwhitelist";
    private static final String KDL_ORDER_ID = "915341030956045";
    private static final String KDL_API_KEY = "u4wkq6pd1528rjsegpm1081k3876pgr6";
    private static final String KUAIDAILI = "KUAIDAILI";


    private static final String GET_ZHIMA_PROXY_URL = "http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=PROVINCE_REPLACE&city=0&yys=0&port=1&pack=49399&ts=0&ys=0&cs=0&lb=6&sb=#&pb=45&mr=3&regions=";
    private static final String ZHIMA_WHITE_LIST_URL = "web.http.cnapi.cc/index/index/save_white?neek=68222&appkey=1f5444badb015126fd0edf9d6818e95d&white=IP_REPLACE";
    private static final String ZHIMA_DEL_WHITE_URL = "web.http.cnapi.cc/index/index/del_white?neek=68222&appkey=1f5444badb015126fd0edf9d6818e95d&white=IP_REPLACE";
    private static final String ZHIMA = "ZHIMA";
    /**
     * 因为学校拨号登陆使用了DHCP，所以每次拨号外网ip都不同
     * 需要把当前ip添加至快代理的白名单
     */
    public static void setKDLWhiteList() {
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("orderid", KDL_ORDER_ID));
        list.add(new BasicNameValuePair("iplist", getLocalIp()));
        list.add(new BasicNameValuePair("signature", KDL_API_KEY));
        HttpUtil.postForm(KDL_WHITE_LIST_URL, list);
    }

    public static void setZHIMAWhiteList() {
        String url = ZHIMA_WHITE_LIST_URL.replace("IP_REPLACE", getLocalIp());
        try {
            HttpUtil.getRequest(url, new ArrayList<>());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ProxyIp getOneProxyWithProvince(String province) {
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
            url = GET_KDL_PROXY_URL.replace("AREA_REPLACE", province);

        } else if (ZHIMA.equals(brand)) {
            String provinceId = getProvinceId(province);
            if (provinceId == null) {
                return null;
            }
            url = GET_ZHIMA_PROXY_URL.replace("PROVINCE_REPLACE", provinceId);
        } else {
            throw new RuntimeException("getProxy()参数错误 brand = " + brand);
        }
        String result = HttpUtil.getRequest(url);
        log.info("{} return: [{}]", brand, result);
        if (result.contains("ERROR") || result.contains("\"success\":false")) {
            waitSeconds(1);
            return null;
        }
        ProxyIp proxyIp = new ProxyIp();
        int index = result.indexOf(":");
        if (index == -1) {
            return null;
        }
        String ip = result.substring(0, index);
        String port = result.substring(index + 1);
        proxyIp.setIp(ip);
        proxyIp.setPort(Integer.parseInt(port));
        proxyIp.setLocation(province);
        proxyIp.setAvailable(true);
        log.info("{}: {}", brand, proxyIp);
        return proxyIp;
    }


    private static String getLocalIp(){
        InetAddress ia;
        try {
            ia = InetAddress.getLocalHost();

            String localName = ia.getHostName();
            String localIp = ia.getHostAddress();
            System.out.println("本机名称是:" + localName);
            System.out.println("本机的ip是:" + localIp);
            return localIp;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("获取本机ip失败");
    }


    public static boolean isValid(ProxyIp proxyIp){
        //Proxy类代理方法
        URL url;
        InetSocketAddress addr;
        try {
            url = new URL("http://www.baidu.com");
            // 创建代理服务器
            addr = new InetSocketAddress(proxyIp.getIp(), proxyIp.getPort());
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr); // http 代理
            URLConnection httpCon = url.openConnection(proxy);
            httpCon.setConnectTimeout(5000);
            httpCon.setReadTimeout(5000);
            InputStream in = httpCon.getInputStream();
            String s = convertStreamToString(in);
            if (s.indexOf("百度") > 0){
                System.out.println("ip可用");
                return true;
            }

        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("ip不可用");
        }

        return false;
    }

    public static String convertStreamToString(InputStream is) {
        if (is == null)
            return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
        } catch (SocketTimeoutException e){
            log.warn(e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();

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
