package com.eastcom.ecfc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LoginController
 *
 * @author zy
 * @version 2022/5/5
 */
@RestController
@Slf4j
public class LoginController {

    @GetMapping("/permit")
    public String doLogin() {
        return "success";
    }
}
