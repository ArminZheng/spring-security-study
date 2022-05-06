package com.eastcom.ecfc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LoginController
 *
 * @author zy
 * @version 2022/5/5
 */
@RestController
public class LoginController {

    @GetMapping("doLogin")
    public String doLogin(){
        return "success";
    }
}
