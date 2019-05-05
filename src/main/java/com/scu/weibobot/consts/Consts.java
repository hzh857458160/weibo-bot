package com.scu.weibobot.consts;

public class Consts {


    public static final String[] PROVINCE = {"北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林",
            "黑龙江", "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南",
            "广东", "广西", "海南", "重庆", "四川", "贵州", "云南", "西藏", "陕西", "甘肃", "青海",
            "宁夏", "新疆", "台湾", "香港", "澳门"};
    public static final int N_LEVEL = 9;
    public static final int H_LEVEL = 58 + 9;
    public static final int VH_LEVEL = 33 + 58 + 9;

    public static final String[] BOT_LEVEL = {"N", "H", "VH"};
    public static final double[] BOT_LEVEL_PROB = {0.5, 0.7, 0.9};

    public static final String[] INTERESTS_GIRL = {"瑜伽", "烘焙", "舞蹈", "明星", "美妆"};
    public static final String[] INTERESTS_NEUTRAL = {"唱歌", "生活", "搞笑", "经济", "艺术", "阅读", "美食",
            "动漫", "摄影", "文化", "旅行", "金融", "游戏", "健康", "设计", "自然", "法律", "电影",
            "音乐", "综艺", "小说", "投资", "电视剧", "综艺", "家居"};
    public static final String[] INTERESTS_BOY = { "运动", "互联网", "足球", "汽车", "篮球", "电脑", "体育",
            "科学", "美女", "手机", "耳机", "下棋", "魔术", "搏击", "滑板", "嘻哈", "绘画"};

    public static final String[] NICKNAME_SUFFIX = {"a", "b", "c", "d", "e","f", "g", "h", "i", "j", "k",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "-", "_", "1",
            "2", "3", "4", "5", "6", "7", "8", "9", "0"};

//    public static final String MOBILE_USER_AGENT = "user-agent = 'Mozilla/5.0 (Linux; U; Android 8.1.0; zh-CN; " +
//            "BLA-AL00 Build/HUAWEIBLA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 " +
//            "UCBrowser/11.9.4.974 UWS/2.13.1.48 Mobile Safari/537.36 AliApp(DingTalk/4.5.11) " +
//            "com.alibaba.android.rimet/10487439 Channel/227200 language/zh-CN'";

    //每到整点会运行
    public static final String RUN_PER_HOUR_CRON = " 0 37 * * *  ?";

    //不同使用度之间的时间点数
    public static final int[] VERY_HIGH_ACTIVE_TIME = {12, 13, 14, 19, 20, 21, 22, 23};

    public static final int[] HIGH_ACTIVE_TIME = {10, 11, 18, 0};

    public static final int[] NORMAL_ACTIVE_TIME = {7, 8, 9, 15, 16, 17};

    public static final int[] LOW_ACTIVE_TIME = {1};

    public static final int[] VERY_LOW_ACTIVE_TIME = {2, 3, 4, 5, 6};

    //
    public static final double VH_ACTIVE_PROB = 0.9;
    public static final double H_ACTIVE_PROB = 0.7;
    public static final double N_ACTIVE_PROB = 0.5;
    public static final double L_ACTIVE_PROB = 0.3;
    public static final double VL_ACTIVE_PROB = 0.1;


    public static final int AGE_LESS_THAN_15 = 1;
    public static final int AGE_BETWEEN_15_AND_19 = 7 + 1;
    public static final int AGE_BETWEEN_20_AND_24 = 33 + 7 + 1;
    public static final int AGE_BETWEEN_25_AND_29 = 28 + 33 + 7 + 1;
    public static final int AGE_BETWEEN_30_AND_39 = 26 + 28 + 33 + 7 + 1;
    public static final int AGE_MORE_THAN_40 = 5 + 26 + 28 + 33 + 7 + 1;

    public static final int BOY_PROB = 43;
    public static final int GIRL_PROB = 57;

    public static final int POST_WEIBO_PROB = 40;
    public static final int LIKE_WEIBO_PROB = 30;
    public static final int REPORT_WEIBO_PROB = 20;
    public static final int COMMENT_WEIBO_PROB = 20;


    public static final int[] REGIONS = {110000, 130000, 140000, 150000, 210000,
            310000, 320000, 330000, 340000, 350000, 360000, 370000, 410000,
            420000, 430000, 440000, 500000, 510000, 530000, 610000, 620000};

    public static final String[] REGIONS_PROVINCE = {"北京", "河北", "山西", "内蒙古", "辽宁", "上海",
            "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南",
            "广东", "重庆", "四川", "云南", "陕西", "甘肃"};
}
