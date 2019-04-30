package com.scu.weibobot.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ClassName:HttpResult
 * ClassDesc: TODO
 * Author: HanrAx
 * Date: 2019/04/27
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HttpResult {
    /**
     * 响应状态码
     */
    private int code;
    /**
     * 响应数据
     */
    private String content;

    public HttpResult(int code) {
        this.code = code;
    }
}
