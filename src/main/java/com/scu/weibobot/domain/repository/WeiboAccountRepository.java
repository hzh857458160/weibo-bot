package com.scu.weibobot.domain.repository;

import com.scu.weibobot.domain.WeiboAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeiboAccountRepository extends JpaRepository<WeiboAccount, Long> {

    WeiboAccount findByUsername(String username);
}
