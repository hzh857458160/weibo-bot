package com.scu.weibobot.utils;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class VerifyCodeUtil {

    public static void handleVerifyCode(WebDriver driver){
        WebElement ensureBtn = WebDriverUtil.isElementExist(By.cssSelector("span.geetest_radar_tip_content"), driver);
        if (ensureBtn == null){
            log.info("验证按钮不存在");
            return;
        }
        ensureBtn.click();



    }
}
