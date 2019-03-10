package com.scu.weibobot.utils;

import com.scu.weibobot.domain.Consts;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class TimeUtil {

    public static double getProbability(String botLevel){
        int index = -1;
        switch (botLevel){
            case "N":
                index = 2;
                break;
            case "H":
                index = 1;
                break;
            case "VH":
                index = 0;
                break;
        }
        double prob = 0.0;
        int hour = LocalTime.now().getHour();
        List<Integer> nList = IntStream.of(Consts.NORMAL_CLOCK).boxed().collect(Collectors.toList());
        List<Integer> hList = IntStream.of(Consts.HIGH_CLOCK).boxed().collect(Collectors.toList());
        List<Integer> vhList = IntStream.of(Consts.VERY_HIGH_CLOCK).boxed().collect(Collectors.toList());

        if (nList.contains(hour)){
            log.info("当前处于轻度时间段", hour);
            prob = Consts.NORMAL_PROBABILITY[index];
        } else if (hList.contains(hour)){
            log.info("当前处于中度时间段", hour);
            prob = Consts.HIGH_PROBABILITY[index];
        } else if (vhList.contains(hour)){
            log.info("当前处于重度时间段", hour);
            prob = Consts.VERY_HIGH_PROBABILITY[index];
        } else {
            log.error("hour error");
        }
        return prob;
    }
}
