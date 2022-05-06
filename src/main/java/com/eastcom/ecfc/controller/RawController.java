package com.eastcom.ecfc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * RawController
 *
 * @author zy
 * @version 2022/5/6
 */
@Controller
public class RawController {

    @RequestMapping("/logout.html")
    public String logout(){
        return "logout";
    }

    @RequestMapping("/login.html")
    public String login(){
        return "login";
    }
}
