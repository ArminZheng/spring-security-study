package com.eastcom.ecfc.controller;

import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

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

    @PostMapping(value = "/post")
    public String withdrawPost(@RequestParam("name") String  name, BindException exception, WebRequest quest) {
        // quest.get
        return "Withdraw Info " + name;
    }
}
