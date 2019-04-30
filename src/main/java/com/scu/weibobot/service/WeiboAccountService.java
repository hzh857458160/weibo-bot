package com.scu.weibobot.service;

import com.scu.weibobot.domain.WeiboAccount;
import com.scu.weibobot.service.repository.WeiboAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<WeiboAccount> findById(Long id) {
        return accountRepository.findById(id);
    }
}
