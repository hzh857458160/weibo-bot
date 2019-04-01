package com.scu.weibobot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PushController {

    @GetMapping("/index")
    public String index() {
        return "home";
    }
}
