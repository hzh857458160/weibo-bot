package com.scu.weibobot.utils;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * ClassName:HttpUtil
 * ClassDesc: Http工具类
 * Author: HanrAx
 * Date: 2019/04/13
 **/
@Slf4j
public class HttpUtil {

    public static String getRequest(String url) {
        HttpGet get = new HttpGet(url);
        HttpClient client = HttpClientBuilder.create().build();
        try {
            HttpResponse response = client.execute(get);
            int code = response.getStatusLine().getStatusCode();
            if (code >= 400) {
                throw new RuntimeException("Could not access protected resource. Server returned http code: " + code);
            }
            return EntityUtils.toString(response.getEntity());

        } catch (ClientProtocolException e) {
            log.error("getRequest -- Client protocol exception!");

        } catch (IOException e) {
            log.error("getRequest -- IO error!");

        } finally {
            get.releaseConnection();
        }
        throw new RuntimeException("getRequest() error");
    }

    // 发送GET请求
    public static String getRequest(String url, List<NameValuePair> parametersBody) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        uriBuilder.setParameters(parametersBody);
        HttpGet get = new HttpGet(uriBuilder.build());
        HttpClient client = HttpClientBuilder.create().build();
        try {
            HttpResponse response = client.execute(get);
            int code = response.getStatusLine().getStatusCode();
            if (code >= 400) {
                throw new RuntimeException("Could not access protected resource. Server returned http code: " + code);
            }
            return EntityUtils.toString(response.getEntity());

        } catch (ClientProtocolException e) {
            log.error("getRequest -- Client protocol exception!");

        } catch (IOException e) {
            log.error("getRequest -- IO error!");

        } finally {
            get.releaseConnection();
        }
        throw new RuntimeException("getRequest() error");
    }

    // 发送POST请求（普通表单形式）
    public static String postForm(String path, List<NameValuePair> parametersBody) {
        HttpEntity entity = new UrlEncodedFormEntity(parametersBody, Charsets.UTF_8);
        return postRequest(path, "application/x-www-form-urlencoded", entity);
    }

    // 发送POST请求（JSON形式）
    public static String postJSON(String path, String json) {
        StringEntity entity = new StringEntity(json, Charsets.UTF_8);
        return postRequest(path, "application/json", entity);
    }

    // 发送POST请求
    public static String postRequest(String path, String mediaType, HttpEntity entity) {
        log.debug("[postRequest] resourceUrl: {}", path);
        HttpPost post = new HttpPost(path);
        post.addHeader("Content-Type", mediaType);
        post.addHeader("Accept", "application/json");
        post.setEntity(entity);
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(post);
            int code = response.getStatusLine().getStatusCode();
            if (code >= 400) {
                String result = EntityUtils.toString(response.getEntity());
                log.error(result);
                throw new RuntimeException(result);
            }
            return EntityUtils.toString(response.getEntity());

        } catch (ClientProtocolException e) {
            log.error("postRequest -- Client protocol exception!");

        } catch (IOException e) {
            log.error("postRequest -- IO error!");

        } finally {
            post.releaseConnection();
        }
        throw new RuntimeException("postRequest() error");
    }
}
