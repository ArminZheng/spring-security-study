package com.eastcom.ecfc.controller;

import org.springframework.web.bind.annotation.*;

/**
 * WithdrawController
 *
 * @author zy
 * @version 2022/5/23
 */
@RestController
@RequestMapping("/withdraw")
// @CrossOrigin 整个类都适用跨域
public class WithdrawController {

    @GetMapping
    public String withdraw() {
        return "Withdraw Success";
    }

    @CrossOrigin
    @GetMapping("/info")
    public String withdrawInfo(@CookieValue("XSRF-TOKEN") String token) {
        return "Withdraw Info " + token;
    }

    @PostMapping("/post")
    public String withdrawPost(@RequestParam("name") String  name) {
        return "Withdraw Info " + name;
    }
}
