package com.scu.weibobot.service;

import com.scu.weibobot.domain.BotInfo;
import com.scu.weibobot.domain.repository.BotInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
