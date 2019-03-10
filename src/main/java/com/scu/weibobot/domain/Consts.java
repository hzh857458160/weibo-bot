package com.scu.weibobot.domain;

public class Consts {

    public static final String[] PROVICE = {"北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林",
            "黑龙江", "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南",
            "广东", "广西", "海南", "重庆", "四川", "贵州", "云南", "西藏", "陕西", "甘肃", "青海",
            "宁夏", "新疆", "台湾", "香港", "澳门"};


    public static final String[] BOT_LEVEL = {"N", "H", "VH"};

    public static final String[] INTERESTS = {"生活方式", "经济学", "运动", "互联网", "艺术", "阅读",
            "美食", "动漫", "汽车", "足球", "教育", "摄影", "历史", "文化", "旅行", "职业发展", "金融",
            "游戏", "篮球", "生物学", "物理", "化学", "科技", "体育", "商业", "健康", "创业", "设计",
            "自然科学", "法律", "电影", "音乐", "投资"};

    public static final String[] NICKNAME_SUFFIX = {"a", "b", "c", "d", "e","f", "g", "h", "i", "j", "k",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "-", "_", "1",
            "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    private static final String MOBILE_USER_AGENT = "user-agent = 'Mozilla/5.0 (Linux; U; Android 8.1.0; zh-CN; " +
            "BLA-AL00 Build/HUAWEIBLA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 " +
            "UCBrowser/11.9.4.974 UWS/2.13.1.48 Mobile Safari/537.36 AliApp(DingTalk/4.5.11) " +
            "com.alibaba.android.rimet/10487439 Channel/227200 language/zh-CN'";

    //每到整点会运行
    public static final String RUN_PER_HOUR_CRON = " 0 29 * * *  ?";

    //不同使用度之间的时间点数
    public static final int[] VERY_HIGH_CLOCK = {12, 13, 14};

    public static final int[] HIGH_CLOCK = {10, 11, 18, 19, 20, 21, 22, 23, 0};

    public static final int[] NORMAL_CLOCK = {1, 2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17};

    //不同人的使用度的概率
    public static final double[] VERY_HIGH_PROBABILITY = {0.8, 0.6, 0.4};
    public static final double[] HIGH_PROBABILITY = {0.6, 0.4, 0.2};
    public static final double[] NORMAL_PROBABILITY = {0.45, 0.25, 0.05};
}
