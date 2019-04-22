package com.scu.weibobot.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProxyIp {

    private Long id;

    private String ip;

    private int port;

    private String location;

    private String type; //类型 http or https

    private boolean available;


}
