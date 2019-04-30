package com.scu.weibobot.service;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.service.repository.BotInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BotInfoService {
    @Autowired
    private BotInfoRepository botInfoRepository;

    public void addBotInfo(BotInfo botInfo){
        botInfoRepository.save(botInfo);
    }

    public BotInfo findBotInfoByAccountId(Long accountId){
        return botInfoRepository.findByAccountId(accountId);
    }

    public List<String> findAllBotLocations(){
        List<BotInfo> botInfoList = botInfoRepository.findAll();
        List<String> locationList = new ArrayList<>();
        for (BotInfo botInfo : botInfoList){
            locationList.add(botInfo.getLocation());
        }
        return locationList;
    }

    public List<BotInfo> finaAll() {
        return botInfoRepository.findAll();
    }

    public Optional<BotInfo> findBotInfoById(Long id) {
        return botInfoRepository.findById(id);
    }

    public void updateStatusByBotId(Long botId, int status) {
        botInfoRepository.updateStatusByBotId(botId, status);
    }
}
