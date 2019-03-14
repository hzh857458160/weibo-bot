package com.scu.weibobot.domain.repository;

import com.scu.weibobot.domain.ProxyIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface ProxyIpRepository extends JpaRepository<ProxyIp, Long> {

    List<ProxyIp> findAllByLocationAndAvailable(String location, boolean available);

    @Query(value = "select * from proxy_ip where location like concat(:province, '%') and available = :flag", nativeQuery = true)
    List<ProxyIp> findAllByProvince(@Param("province") String province, Boolean flag);

    @Modifying
    @Query(nativeQuery = true, value = "update proxy_ip set available = :flag where id = :id")
    void updateAvailableById(Long id, boolean flag);


    void deleteAllByAvailable(boolean available);
}
