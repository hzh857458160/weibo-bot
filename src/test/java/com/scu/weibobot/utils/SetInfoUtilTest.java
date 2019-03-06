package com.scu.weibobot.utils;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class SetInfoUtilTest {

    @Test
    public void setBotInfo() {
        String username = "kgsrchbjaezhmr-vpb2@yahoo.com";
        String password = "WAsbjlttuv03";

        WeiboOpUtil.loginWeibo(username, password);

    }
}