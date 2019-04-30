package com.scu.weibobot.service.repository;

import com.scu.weibobot.domain.BotInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface BotInfoRepository extends JpaRepository<BotInfo, Long> {
    //通过账号id来查找对应的机器人资料
    BotInfo findByAccountId(Long accountId);

    @Modifying
    @Query("update BotInfo b set b.status = ?2 where b.id = ?1")
    void updateStatusByBotId(Long botId, int status);

}
