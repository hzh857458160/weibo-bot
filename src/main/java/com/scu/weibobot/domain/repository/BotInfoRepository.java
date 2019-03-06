package com.scu.weibobot.domain.repository;

import com.scu.weibobot.domain.BotInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotInfoRepository extends JpaRepository<BotInfo, Long> {
    //通过账号id来查找对应的机器人资料
    BotInfo findByAccountId(Long accountId);
}
