package com.armin.security.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

/**
 * WithdrawController
 *
 * @author zy
 * @version 2022/5/23
 */
@RestController
@RequestMapping("/withdraw")
@Validated // 支持验证组 Validation Group，不支持属性校验，spring提供的
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
    public String withdrawPost(@Valid @RequestBody Map<String, String> values, BindingResult bindingResult, WebRequest quest) {
        // quest.get
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("错误提示码" + bindingResult.getFieldError().getDefaultMessage());
        }
        return "Withdraw Info " + values + "\ncurrent: " + getCurrentUserName() + "\n exception" + bindingResult;
    }

    @GetMapping("/currentUser")
    public Authentication getCurrentUser(Authentication authentication) {
        return authentication;
    }

    @GetMapping("/currentUsername")
    public String currentUserName(Principal principal) {
        return principal.getName();
    }

    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName(); // 一般为 loginId
            return currentUserName;
        } else { // 解决匿名访问, 返回一个匿名用户的问题
            throw new RuntimeException("No User");
        }
    }
}
