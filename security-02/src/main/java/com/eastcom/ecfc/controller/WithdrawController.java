package com.eastcom.ecfc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WithdrawController
 *
 * @author zy
 * @version 2022/5/23
 */
@RestController
@RequestMapping("/withdraw")
public class WithdrawController {

    @GetMapping
    public String withdraw() {
        return "Withdraw Success";
    }

    @GetMapping("/info")
    public String withdrawInfo() {
        return "Withdraw Info";
    }

    @PostMapping("/post")
    public String withdrawPost(@RequestParam("name") String  name) {
        return "Withdraw Info " + name;
    }
}
