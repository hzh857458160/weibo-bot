package com.scu.weibobot.domain;

public class Consts {

    public static final String[] PROVINCE = {"北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林",
            "黑龙江", "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南",
            "广东", "广西", "海南", "重庆", "四川", "贵州", "云南", "西藏", "陕西", "甘肃", "青海",
            "宁夏", "新疆", "台湾", "香港", "澳门"};

    public static final String[] PROVINCE_SPELL = {"beijing", "tianjin", "hebei", "shanxi", "neimenggu", "liaonig",
            "jilin",  "heilongjiang", "shanghai", "jiangsu", "zhejiang", "anhui", "fujian", "jiangxi", "shandong",
            "henan", "hubei", "hunan", "guangdong", "guangxi", "hainan", "chongqin", "sichuang", "guizhou",
            "yunnan", "xizang", "shanxi", "gansu", "qinghai", "ningxia", "xinjiang", "taiwan", "xianggang", "aomen"};



    public static final String[] BOT_LEVEL = {"N", "H", "VH"};

    public static final String[] INTERESTS_GIRL = {"瑜伽", "烘焙", "舞蹈", "明星", "美妆", };
    public static final String[] INTERESTS_NEUTRAL = {"唱歌", "生活", "搞笑", "经济", "艺术", "阅读", "美食",
            "动漫", "摄影", "历史", "文化", "旅行", "金融", "游戏", "健康", "设计", "自然", "法律", "电影",
            "音乐", "综艺", "小说", "投资", "小说", "电视剧", "综艺", "家居"};
    public static final String[] INTERESTS_BOY = { "运动", "互联网", "足球", "汽车", "篮球", "电脑", "体育",
            "科学", "美女", "手机", "耳机", "下棋", "魔术", "搏击", "滑板", "说唱", "绘画"};

    public static final String[] NICKNAME_SUFFIX = {"a", "b", "c", "d", "e","f", "g", "h", "i", "j", "k",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "-", "_", "1",
            "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    private static final String MOBILE_USER_AGENT = "user-agent = 'Mozilla/5.0 (Linux; U; Android 8.1.0; zh-CN; " +
            "BLA-AL00 Build/HUAWEIBLA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 " +
            "UCBrowser/11.9.4.974 UWS/2.13.1.48 Mobile Safari/537.36 AliApp(DingTalk/4.5.11) " +
            "com.alibaba.android.rimet/10487439 Channel/227200 language/zh-CN'";

    //每到整点会运行
    public static final String RUN_PER_HOUR_CRON = " 0 15 * * *  ?";
    //每天的0点会运行
    public static final String RUN_PER_DAY_CRON = "0 36 16 * * ?";



    //不同使用度之间的时间点数
    public static final int[] VERY_HIGH_CLOCK = {12, 13, 14};

    public static final int[] HIGH_CLOCK = {10, 11, 18, 19, 20, 21, 22, 23, 0};

    public static final int[] NORMAL_CLOCK = {1, 2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17};

    //不同人的使用度的概率
    public static final double[] VERY_HIGH_PROBABILITY = {0.8, 0.6, 0.4};
    public static final double[] HIGH_PROBABILITY = {0.6, 0.4, 0.2};
    public static final double[] NORMAL_PROBABILITY = {0.45, 0.25, 0.05};
}
