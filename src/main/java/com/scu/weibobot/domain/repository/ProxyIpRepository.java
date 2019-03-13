package com.scu.weibobot.domain.repository;

import com.scu.weibobot.domain.ProxyIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProxyIpRepository extends JpaRepository<ProxyIp, Long> {

    List<ProxyIp> findAllByLocationAndAvailable(String location, boolean available);
}
