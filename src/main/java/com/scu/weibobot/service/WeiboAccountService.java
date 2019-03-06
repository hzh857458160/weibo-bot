package com.scu.weibobot.service;

import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.domain.repository.WeiboAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeiboAccountService {
    @Autowired
    private WeiboAccountRepository accountRepository;

    public void addWeiboAccount(WeiboAccount weiboAccount){
        accountRepository.save(weiboAccount);
    }

    public WeiboAccount findByUsername(String username){
        return accountRepository.findByUsername(username);
    }

    public List<WeiboAccount> findAllAccount(){
        return accountRepository.findAll();
    }
}
