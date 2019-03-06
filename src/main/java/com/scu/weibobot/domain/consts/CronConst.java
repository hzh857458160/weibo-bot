package com.scu.weibobot.domain.consts;

public class CronConst {
//    //重度时间段
//    public static final String VH_CLOCK_CRON = "0 0 10,11,12,13,14,23,0 * * ?";
//    //中度时间段
//    public static final String H_CLOCK_CRON = "0 0 15,16,17,18,1,2,3,4,5 * * ?";
//    //轻度时间段
//    public static final String N_CLOCK_CRON = "0 0 6,7,8,9 * * ?";
    //每到整点会运行
    public static final String RUN_PER_HOUR_CRON = " 0 20 * * *  ?";

    //不同使用度之间的时间点数
    public static final int[] VERY_HIGH_CLOCK = {10, 11, 12, 13, 14, 23, 0 };

    public static final int[] HIGH_CLOCK = {15, 16, 17, 18, 1, 2, 3, 4, 5 };

    public static final int[] NORMAL_CLOCK = {6, 7, 8, 9 };

    //不同人的使用度的概率
    public static final double[] VERY_HIGH_PROBABILITY = {0.8, 0.6, 0.4};
    public static final double[] HIGH_PROBABILITY = {0.6, 0.4, 0.2};
    public static final double[] NORMAL_PROBABILITY = {0.45, 0.25, 0.05};


}
