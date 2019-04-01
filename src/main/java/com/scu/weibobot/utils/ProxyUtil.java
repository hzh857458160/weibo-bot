package com.scu.weibobot.utils;

import com.scu.weibobot.domain.pojo.ProxyIp;
import com.scu.weibobot.taskexcuter.WebDriverPool;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
    private static final String GET_KUAIDAILI_PROXY_URL = "http://dps.kdlapi.com/api/getdps/?orderid=915341030956045&num=NUM_REPLACE&area=AREA_REPLACE&pt=1&ut=1&f_loc=1&sep=%23";
    private static final String KUAIDAILI_URL = "https://www.kuaidaili.com/";
    private static final String KUAIDAILI_KEY = "KUAIDAILI";

//    private static final String NO_SUCH_PROXY_TIPS = "没有找到符合条件的代理，请稍候再试。";

    private static String CURRENT_IP = "220.167.41.229";


    /**
     * 因为学校拨号登陆使用了DHCP，所以每次拨号外网ip都不同
     * 需要把当前ip添加至快代理的白名单
     */
    private static void setLocalIpToWhiteListInKUAIDAILI(){
        WebDriver driver = null;
        try{
            driver = WebDriverPool.getWebDriver();
            if (WebDriverUtil.getUrlWithCookie(driver, KUAIDAILI_URL, KUAIDAILI_KEY)) {

                driver.findElement(By.cssSelector("a.qc-btn.link-name.welcome-link")).click();

            } else {
                driver.get(KUAIDAILI_URL);
                waitSeconds(3);

                WebDriverUtil.forceGetElement(By.cssSelector("a.qc-btn.link-dl"), driver).click();
                waitSeconds(2);

                WebElement usernameInput = WebDriverUtil.forceGetElement(By.id("username"), driver);
                usernameInput.clear();
                usernameInput.click();
                usernameInput.sendKeys("hzh00112@163.com");
                waitSeconds(2);

                WebElement passwordInput = WebDriverUtil.forceGetElement(By.id("passwd"), driver);
                passwordInput.clear();
                passwordInput.click();
                passwordInput.sendKeys("M5RWHVBL5ktJFn6");
                waitSeconds(1);

                WebDriverUtil.forceGetElement(By.id("postcontent"), driver).click();
                waitSeconds(2);
            }
            WebDriverUtil.forceGetElement(By.id("ucm_dpsipwhitelist"), driver).click();
            waitSeconds(2);
            String myIp = driver.findElement(By.id("myIP")).getText();
            CURRENT_IP = myIp;
            WebElement whiteListTextarea = WebDriverUtil.forceGetElement(By.id("iplist"), driver);
            whiteListTextarea.clear();
            whiteListTextarea.click();
            whiteListTextarea.sendKeys(myIp);
            WebDriverUtil.forceGetElement(By.id("postcontent"), driver).click();
            WebDriverUtil.saveCurrentCookies(driver, KUAIDAILI_KEY, 24 * 60 * 60);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public static synchronized ProxyIp getOneProxyWithLocation(String location) {
        List<ProxyIp> ipList = getProxyFromKUAIDAILI(1, location);
        return ipList == null ? null : ipList.get(0);
    }


    private static List<ProxyIp> getProxyFromKUAIDAILI(int num, String... locations) {

//        if (!CURRENT_IP.equals(getLocalIp())){
//            CURRENT_IP = getLocalIp();
//            setLocalIpToWhiteListInKUAIDAILI();
//        }
        if (num < 1 || locations == null){
            return null;
        }
        List<ProxyIp> ipList = new ArrayList<>();
        StringBuilder locationSb = new StringBuilder();
        for (String loc : locations){
            locationSb.append(loc).append(",");
        }
        locationSb.setLength(locationSb.length() - 1);
        String url = GET_KUAIDAILI_PROXY_URL.replace("NUM_REPLACE", num + "")
                .replace("AREA_REPLACE", locationSb);
        String result = sendGet(url);
        log.info("KUAI return ：{}", result);
        for (String tempIp : result.split("#")){
            ProxyIp proxyIp = new ProxyIp();
            int index1 = tempIp.indexOf(":");
            int index2 = tempIp.indexOf(",");
            if (index1 == -1 || index2 == -1) {
                return null;
            }
            String ip = tempIp.substring(0, index1);
            String port = tempIp.substring(index1 + 1, index2);
            String location = tempIp.substring(index2 + 1);
            proxyIp.setIp(ip);
            proxyIp.setPort(Integer.parseInt(port));
            proxyIp.setLocation(location);
            proxyIp.setAvailable(true);
            log.info("KUAI独享代理:" + proxyIp);
            ipList.add(proxyIp);
        }
        return ipList;
    }

    /**
     * 向指定URL发送GET方式的请求
     * @param url  发送请求的URL
     * @return URL 代表远程资源的响应
     */
    public static String sendGet(String url){
        String result = "";
        try{
            URL realUrl = new URL(url);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //建立实际的连接
            conn.connect();
//            //获取所有的响应头字段
//            Map<String,List<String>> map = conn.getHeaderFields();
//            //遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "-->" + map.get(key));
//            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            log.error("发送GET请求出现异常" + e);
            waitSeconds(2);
            e.printStackTrace();
        }
        return result;


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


//    public static List<ProxyIp> crawlFromKUAIDAILI(WebDriver driver){
//        log.info("进入crawlFromKUAIDAILI()");
//        int page = 1;
//        int capacity = 10;
//        List<ProxyIp> ipList = new ArrayList<>(capacity);
//        long startTime = System.currentTimeMillis();
//        while (ipList.size() < capacity){
//            if (startTime + 15 * 60 * 1000 < System.currentTimeMillis()){
//                break;
//            }
//            String tempUrl = KUAIDAILI_PROXY_URL.replace("PAGE_REPLACE", page + "");
//            driver.get(tempUrl);
//            waitSeconds(2);
//            List<WebElement> list = driver.findElements(By.cssSelector("tbody > tr"));
//            System.out.println("list size = " + ipList.size());
//            if (list.size() > 0){
//                for (int i = 0; i < list.size(); i++){
//                    WebElement ipLine = list.get(i);
//                    String location = ipLine.findElement(By.cssSelector("td[data-title=\"位置\"]")).getText();
////                    System.out.println(location);
//                    location = parseLocation(location);
//                    if (!locationCheck(location)){
//                        continue;
//                    }
//                    String ip = ipLine.findElement(By.cssSelector("td[data-title=\"IP\"]")).getText();
//                    String port = ipLine.findElement(By.cssSelector("td[data-title=\"PORT\"]")).getText();
//                    String type = ipLine.findElement(By.cssSelector("td[data-title=\"类型\"]")).getText();
//                    System.out.println("当前ip信息为：" + ip + ", " + port + ", " + location + ", " + type);
//                    log.info("当前ip信息为：[{},{},{},{}]", ip, port, location, type);
//
//                    ProxyIp mProxyIp = new ProxyIp();
//                    mProxyIp.setIp(ip);
//                    mProxyIp.setPort(Integer.parseInt(port));
//                    mProxyIp.setLocation(location);
//                    mProxyIp.setType(type);
//
//                    if (isValid(mProxyIp)){
//                        System.out.println(i + " ip可用");
//                        mProxyIp.setAvailable(true);
//                        ipList.add(mProxyIp);
//                    }
//
//                }
//            }
//
//            page++;
//            waitSeconds(1);
//
//        }
//        return ipList;
//    }
//
//    public static String parseLocation(String location){
//        String province;
//        String city = "";
//        location = location.replace("中国", "");
//        int indexProvince = location.indexOf("省");
//        int indexCity = location.indexOf("市");
//        int indexArea = location.indexOf("自治区");
//        if (indexCity != -1){
//            if (indexProvince != -1){
//                //带省带市
//                province =  location.substring(0, indexProvince).trim();
//                city =  location.substring(indexProvince + 1, indexCity).trim();
//
//            } else if (indexArea != -1){
//                //带自治区带市
//                if (location.contains("内蒙古")){
//                    province = "内蒙古";
//                } else {
//                    province = location.substring(0, 2).trim();
//                }
//                city = location.substring(indexArea + 3, indexCity).trim();
//
//            } else {
//                //只带市
//                province = location.substring(0, indexCity).trim();
//            }
//        } else if (indexProvince != -1){
//            ////只带省不带市
//            province = location.substring(0, 2).trim();
//
//        } else if (indexArea != -1){
//            //只带自治区
//            if (location.contains("内蒙古")){
//                province = "内蒙古";
//            } else {
//                province = location.substring(0, 2).trim();
//            }
//        } else {
//            //什么都不带
//            System.out.println(location);
//            String[] list = location.split(" ");
//            if (list.length < 3){
//                return "";
//            }
//            province = list[1];
//            city = list[2];
//            if (province.equals(city)){
//                city = "";
//            }
//        }
//
//        return province + city;
//    }
//
//    public static List<ProxyIp> crawlFrom89IP(WebDriver driver, String location){
//        log.info("进入crawlFrom89IP(location = {})");
//        List<ProxyIp> ipList = new ArrayList<>(30);
//        String province = getProvinceFromLocation(location);
//        String tempUrl = IP89_PROXY_URL.replace("ADDRESS_REPLACE", province);
//
//        driver.get(tempUrl);
//        waitSeconds(3);
//        String text = driver.findElement(By.cssSelector("h1[align = 'center'] + hr + div")).getText();
//        String[] lines = text.split("\n");
//        for (String line : lines){
//            if (line.contains("高效高匿名代理IP提取地址")){
//                continue;
//            }
//            int index = line.indexOf(":");
//            ProxyIp mProxyIp = new ProxyIp();
//            mProxyIp.setIp(line.substring(0, index));
//            mProxyIp.setPort(Integer.parseInt(line.substring(index + 1)));
//            mProxyIp.setLocation(province);
//            if (isValid(mProxyIp)){
//                mProxyIp.setAvailable(true);
//                ipList.add(mProxyIp);
//            }
//        }
//        return ipList;
//    }
//
//    public static List<ProxyIp> crawlFrom89IP(WebDriver driver){
//        log.info("进入crawlFrom89IP()");
//        List<ProxyIp> ipList = new ArrayList<>(50);
//        for (String location : PROVINCE_LIST){
//            ipList.addAll(crawlFrom89IP(driver, location));
//        }
//        return ipList;
//    }
//
//    public static List<ProxyIp> crawlFromZDAYE(WebDriver driver){
//        log.info("进入crawlFromZDAYE()");
//        List<ProxyIp> ipList = new ArrayList<>(50);
//
//        for (int i = 0; i < 15; i++){
//            driver.get(ZDAYE_PROXY_URL);
//            waitSeconds(3);
//            WebElement article = driver.findElement(By.cssSelector("div.table.table-hover.panel-default.panel.ips" +
//                    ":nth-child(" + (i + 2) + ") > div.title > a"));
//            article.click();
//            waitSeconds(3);
//            String text = driver.findElement(By.cssSelector("div.cont")).getText();
//            String[] lines = text.split("\n");
//            for (String line : lines){
//                int index1 = line.indexOf(":");
//                int index2 = line.indexOf("@");
//                int index3 = line.indexOf("#");
//                int index4 = line.indexOf("]");
//                String location = line.substring(index4 + 1);
//                location = parseLocation(location);
//                if (!locationCheck(location)){
//                    continue;
//                }
//                String ip = line.substring(0, index1);
//                String port = line.substring(index1 + 1 , index2);
//                String type = line.substring(index2 + 1, index3);
//
//                ProxyIp mProxyIp = new ProxyIp();
//                mProxyIp.setIp(ip);
//                mProxyIp.setPort(Integer.parseInt(port));
//                mProxyIp.setLocation(location);
//                mProxyIp.setType(type);
//                if (isValid(mProxyIp)){
//                    mProxyIp.setAvailable(true);
//                    ipList.add(mProxyIp);
//                }
//
//            }
//        }
//        return ipList;
//    }
//
//    public static List<ProxyIp> crawlFromQIYUN(WebDriver driver){
//        log.info("进入crawlFromQIYUN()");
//        List<ProxyIp> ipList = new ArrayList<>(25);
//        int page = 1;
//        while(ipList.size() < 25){
//            String tempUrl = QIYUN_PROXY_URL.replace("PAGE_REPLACE", page + "");
//            driver.get(tempUrl);
//            waitSeconds(3);
//
//            List<WebElement> lineList = driver.findElements(By.cssSelector("tbody > tr"));
//            for (WebElement line : lineList){
//                String location = line.findElement(By.cssSelector("td[data-title = '位置']")).getText();
////                System.out.println(location);
//                location  = parseLocationInQIYUN(location);
//                if (!locationCheck(location)){
//                    continue;
//                }
//                String ip = line.findElement(By.cssSelector("td[data-title = 'IP']")).getText();
//                String port = line.findElement(By.cssSelector("td[data-title = 'PORT']")).getText();
//                String type = line.findElement(By.cssSelector("td[data-title = '类型']")).getText();
//
//                ProxyIp mProxyIp = new ProxyIp();
//                mProxyIp.setIp(ip);
//                mProxyIp.setPort(Integer.parseInt(port));
//                mProxyIp.setLocation(location);
//                mProxyIp.setType(type);
//
//                if (isValid(mProxyIp)){
//                    mProxyIp.setAvailable(true);
//                    ipList.add(mProxyIp);
//                }
//
//            }
//
//            page++;
//            waitSeconds(1);
//        }
//        return ipList;
//    }
//
//    private static String parseLocationInQIYUN(String location){
//        String province;
//        String city = "";
//        if (location.length() <= 6){
//            province = location.replace("中国", "").trim();
//        } else {
//            province = location.substring(3, 6).trim();
//            city = location.replace(province, "").replace("中国", "").trim();
//            if (!"".equals(city)){
//                city = city.substring(0, 2);
//            }
//
//        }
//
//        return province + city;
//    }


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
            String s = convertStreamToString(in);;
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
