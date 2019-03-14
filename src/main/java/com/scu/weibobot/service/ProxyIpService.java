package com.scu.weibobot.service;

import com.scu.weibobot.domain.ProxyIp;
import com.scu.weibobot.domain.repository.ProxyIpRepository;
import com.scu.weibobot.utils.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProxyIpService {
    @Autowired
    private ProxyIpRepository proxyIpRepo;

    public void addProxyIp(ProxyIp proxyIp){
        log.info("进入addProxyIp({})", proxyIp);
        proxyIpRepo.save(proxyIp);
    }

    public List<ProxyIp> findAllByLocation(String location){
        log.info("进入findAllByLocation(location = {})", location);
        log.info("先寻找是否有地区的代理");
        List<ProxyIp> list = proxyIpRepo.findAllByLocationAndAvailable(location, true);
        log.info("获取到可用的地区代理", list);
        if (list != null){
            if (list.size() != 0){
                log.info("list可用，返回");
                return list;
            }
        }
        String province = ProxyUtil.getProvinceFromLocation(location);
        log.info("再寻找是否有省会代理 {}", province);
        return proxyIpRepo.findAllByProvince(province, true);
    }

    public void addAllProxyIp(List<ProxyIp> proxyIpList){
        log.info("进入addAllProxyIp(proxyIpList.size() = {})", proxyIpList.size());
        proxyIpRepo.saveAll(proxyIpList);
    }

    public void setInvalidProxy(Long id){
        log.info("进入setInvalidProxy(id = {})", id);
        proxyIpRepo.updateAvailableById(id, false);
    }

    public void deleteAllUselessProxy(){
        log.info("进入deleteAllUselessProxy");
        proxyIpRepo.deleteAllByAvailable(false);
    }

}
