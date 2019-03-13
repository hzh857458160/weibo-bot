package com.scu.weibobot.utils;

import com.scu.weibobot.domain.ProxyIp;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.scu.weibobot.utils.WebDriverUtil.waitSeconds;

@Slf4j
public class ProxyUtil {
    private static final String ZDAYE_PROXY_URL = "http://ip.zdaye.com/dayProxy.html";
    private static final String QIYUN_PROXY_URL = "http://www.qydaili.com/free/?action=china&page=PAGE_REPLACE";
    private static final String IP89_PROXY_URL = "http://www.89ip.cn/tqdl.html?num=30&address=ADDRESS_REPLACE&kill_address=&port=&kill_port=&isp=";
    private static final String KUAIDAILI_PROXY_URL = "https://www.kuaidaili.com/free/inha/PAGE_REPLACE";

    //TODO:后期将从数据库读取所有账号地址
    private static List<String> LOCATION_LIST = null;
    private static List<String> PROVINCE_LIST = null;

    public static void initProxyLocation(List<String> locationList){
        LOCATION_LIST = locationList;
        PROVINCE_LIST = new ArrayList<>();
        for (String location : locationList){
            PROVINCE_LIST.add(getProvinceFromLocation(location));
        }
    }


    public static List<ProxyIp> crawlFromKUAIDAILI(WebDriver driver){
        int page = 20;
        int capacity = 20;
        List<ProxyIp> ipList = new ArrayList<>(capacity);
        long startTime = System.currentTimeMillis();
        while (ipList.size() < capacity){
            if (startTime + 15 * 60 * 1000 < System.currentTimeMillis()){
                break;
            }
            String tempUrl = KUAIDAILI_PROXY_URL.replace("PAGE_REPLACE", page + "");
            driver.get(tempUrl);
            waitSeconds(2);
            List<WebElement> list = driver.findElements(By.cssSelector("tbody > tr"));
            System.out.println("list size = " + ipList.size());
            if (list.size() > 0){
                for (int i = 0; i < list.size(); i++){
                    WebElement ipLine = list.get(i);
                    String location = ipLine.findElement(By.cssSelector("td[data-title=\"位置\"]")).getText();
                    System.out.println(location);
                    location = parseLocation(location);
                    if (!locationCheck(location)){
                        continue;
                    }
                    String ip = ipLine.findElement(By.cssSelector("td[data-title=\"IP\"]")).getText();
                    String port = ipLine.findElement(By.cssSelector("td[data-title=\"PORT\"]")).getText();
                    String type = ipLine.findElement(By.cssSelector("td[data-title=\"类型\"]")).getText();
                    System.out.println("当前ip信息为：" + ip + ", " + port + ", " + location + ", " + type);
                    log.info("当前ip信息为：[{},{},{},{}]", ip, port, location, type);

                    ProxyIp mProxyIp = new ProxyIp();
                    mProxyIp.setIp(ip);
                    mProxyIp.setPort(Integer.parseInt(port));
                    mProxyIp.setLocation(location);
                    mProxyIp.setType(type);

                    if (isValid(mProxyIp)){
                        System.out.println(i + " ip可用");
                        mProxyIp.setAvailable(true);
                        ipList.add(mProxyIp);
                    }

                }
            }

            page++;
            waitSeconds(1);

        }
        return ipList;
    }

    public static String parseLocation(String location){
        String province;
        String city = "";
        location = location.replace("中国", "");
        int indexProvince = location.indexOf("省");
        int indexCity = location.indexOf("市");
        int indexArea = location.indexOf("自治区");
        if (indexCity != -1){
            if (indexProvince != -1){
                //带省带市
                province =  location.substring(0, indexProvince).trim();
                city =  location.substring(indexProvince + 1, indexCity).trim();

            } else if (indexArea != -1){
                //带自治区带市
                if (location.contains("内蒙古")){
                    province = "内蒙古";
                } else {
                    province = location.substring(0, 2).trim();
                }
                city = location.substring(indexArea + 3, indexCity).trim();

            } else {
                //只带市
                province = location.substring(0, indexCity).trim();
            }
        } else if (indexProvince != -1){
            ////只带省不带市
            province = location.substring(0, 2).trim();

        } else if (indexArea != -1){
            //只带自治区
            if (location.contains("内蒙古")){
                province = "内蒙古";
            } else {
                province = location.substring(0, 2).trim();
            }
        } else {
            //什么都不带
            System.out.println(location);
            String[] list = location.split(" ");
            if (list.length < 3){
                return "";
            }
            province = list[1];
            city = list[2];
            if (province.equals(city)){
                city = "";
            }
        }

        return province + city;
    }


    public static List<ProxyIp> crawlFrom89IP(WebDriver driver){
        List<ProxyIp> ipList = new ArrayList<>(50);
        for (String location : PROVINCE_LIST){
            String province = getProvinceFromLocation(location);
            String tempUrl = IP89_PROXY_URL.replace("ADDRESS_REPLACE", province);

            driver.get(tempUrl);
            waitSeconds(3);
            String text = driver.findElement(By.cssSelector("h1[align = 'center'] + hr + div")).getText();
            String[] lines = text.split("\n");

            for (String line : lines){
                if (line.contains("高效高匿名代理IP提取地址")){
                    continue;
                }
                int index = line.indexOf(":");
                ProxyIp mProxyIp = new ProxyIp();
                mProxyIp.setIp(line.substring(0, index));
                mProxyIp.setPort(Integer.parseInt(line.substring(index + 1)));
                mProxyIp.setLocation(province);
                if (isValid(mProxyIp)){
                    mProxyIp.setAvailable(true);
                    ipList.add(mProxyIp);
                }
            }
        }

        return ipList;
    }

    public static List<ProxyIp> crawlFromZDAYE(WebDriver driver){
        List<ProxyIp> ipList = new ArrayList<>(50);

        for (int i = 0; i < 15; i++){
            driver.get(ZDAYE_PROXY_URL);
            waitSeconds(3);
            WebElement article = driver.findElement(By.cssSelector("div.table.table-hover.panel-default.panel.ips" +
                    ":nth-child(" + (i + 2) + ") > div.title > a"));
            article.click();
            waitSeconds(3);
            String text = driver.findElement(By.cssSelector("div.cont")).getText();
            String[] lines = text.split("\n");
            for (String line : lines){
                int index1 = line.indexOf(":");
                int index2 = line.indexOf("@");
                int index3 = line.indexOf("#");
                int index4 = line.indexOf("]");
                String location = line.substring(index4 + 1);
                location = parseLocation(location);
                if (!locationCheck(location)){
                    continue;
                }
                String ip = line.substring(0, index1);
                String port = line.substring(index1 + 1 , index2);
                String type = line.substring(index2 + 1, index3);

                ProxyIp mProxyIp = new ProxyIp();
                mProxyIp.setIp(ip);
                mProxyIp.setPort(Integer.parseInt(port));
                mProxyIp.setLocation(location);
                mProxyIp.setType(type);
                if (isValid(mProxyIp)){
                    mProxyIp.setAvailable(true);
                    ipList.add(mProxyIp);
                }

            }
        }
        return ipList;
    }

    public static List<ProxyIp> crawlFromQIYUN(WebDriver driver){
        List<ProxyIp> ipList = new ArrayList<>(25);
        int page = 1;
        while(ipList.size() < 25){
            String tempUrl = QIYUN_PROXY_URL.replace("PAGE_REPLACE", page + "");
            driver.get(tempUrl);
            waitSeconds(3);

            List<WebElement> lineList = driver.findElements(By.cssSelector("tbody > tr"));
            for (WebElement line : lineList){
                String location = line.findElement(By.cssSelector("td[data-title = '位置']")).getText();
                System.out.println(location);
                location  = parseLocationInQIYUN(location);
                if (!locationCheck(location)){
                    continue;
                }
                String ip = line.findElement(By.cssSelector("td[data-title = 'IP']")).getText();
                String port = line.findElement(By.cssSelector("td[data-title = 'PORT']")).getText();
                String type = line.findElement(By.cssSelector("td[data-title = '类型']")).getText();

                ProxyIp mProxyIp = new ProxyIp();
                mProxyIp.setIp(ip);
                mProxyIp.setPort(Integer.parseInt(port));
                mProxyIp.setLocation(location);
                mProxyIp.setType(type);

                if (isValid(mProxyIp)){
                    mProxyIp.setAvailable(true);
                    ipList.add(mProxyIp);
                }

            }

            page++;
            waitSeconds(1);
        }
        return ipList;
    }

    private static String parseLocationInQIYUN(String location){
        String province;
        String city = "";
        if (location.length() <= 6){
            province = location.replace("中国", "").trim();
        } else {
            province = location.substring(3, 6).trim();
            city = location.replace(province, "").replace("中国", "").trim();
            if (!"".equals(city)){
                city = city.substring(0, 2);
            }

        }

        return province + city;
    }


    private static boolean isValid(ProxyIp proxyIp){
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
            if(s.indexOf("百度")>0){
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
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
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


    public static boolean locationCheck(String location){
        if ("".equals(location)){
            return false;
        }
        if (LOCATION_LIST.contains(location)){
            return true;
        }
        String province = getProvinceFromLocation(location);
        return PROVINCE_LIST.contains(province);
    }

    public static String getProvinceFromLocation(String location){
        //解析出省会
        String province;
        if (location.contains("内蒙古")){
            province = "内蒙古";
        } else if (location.contains("黑龙江")){
            province = "黑龙江";
        } else {
            province = location.substring(0, 2);
        }

        return province;
    }

}
