package com.scu.weibobot.utils;

import org.junit.Test;

import javax.validation.constraints.AssertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;

public class GenerateInfoUtilTest {

    @Test
    public void generateBotInfo() {
        try {
            System.out.println(GenerateInfoUtil.generateImgSrc());
        } catch (InterruptedException e) {

        }
    }


}