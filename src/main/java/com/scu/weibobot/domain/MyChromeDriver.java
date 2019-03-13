package com.scu.weibobot.domain;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.Set;

public class MyChromeDriver extends ChromeDriver {

    private ProxyIp proxyIp;

    public void setProxyIp(ProxyIp proxyIp){
        this.proxyIp = proxyIp;
    }

    public ProxyIp getProxyIp(){
        return proxyIp;
    }

    @Override
    public void get(String s) {
        super.get(s);
    }

    @Override
    public String getCurrentUrl() {
        return super.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return super.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return super.findElement(by);
    }

    @Override
    public String getPageSource() {
        return getPageSource();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void quit() {
        super.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return super.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return super.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return super.switchTo();
    }

    @Override
    public Navigation navigate() {
        return super.navigate();
    }

    @Override
    public Options manage() {
        return super.manage();
    }
}
