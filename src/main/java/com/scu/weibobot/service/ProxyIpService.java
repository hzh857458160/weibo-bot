package com.scu.weibobot.service;

import com.scu.weibobot.domain.ProxyIp;
import com.scu.weibobot.domain.repository.ProxyIpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ProxyIpService {
    @Autowired
    private ProxyIpRepository proxyIpRepo;

    public void addProxyIp(ProxyIp proxyIp){
        proxyIpRepo.save(proxyIp);
    }

    public List<ProxyIp> findAllByLocationAndAvailable(String location, boolean available){
        return proxyIpRepo.findAllByLocationAndAvailable(location, available);
    }

    public void addAllProxyIp(List<ProxyIp> proxyIpList){
        proxyIpRepo.saveAll(proxyIpList);
    }

}
