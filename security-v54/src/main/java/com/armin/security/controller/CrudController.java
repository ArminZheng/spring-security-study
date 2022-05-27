package com.armin.security.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
public class CrudController {

    @GetMapping("/hello")
    public String withdraw() {
        return "Withdraw Success";
    }

    @CrossOrigin
    @GetMapping("/info")
    public String withdrawInfo(@CookieValue("XSRF-TOKEN") String token) {
        return "Withdraw Info " + token;
    }

    @PostMapping(value = "/post")
    public String withdrawPost(
            @Valid @RequestBody Map<String, String> values,
            BindingResult bindingResult,
            WebRequest quest) {
        Principal userPrincipal = quest.getUserPrincipal();
        // quest.get
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(
                    "错误提示码" + bindingResult.getFieldError().getDefaultMessage());
        }
        return "Withdraw Info "
                + values
                + "\ncurrent: "
                + getCurrentUserName()
                + "\n exception"
                + bindingResult
                + "\n userPrincipal: "
                + userPrincipal;
    }

    @GetMapping("/currentUser")
    public Authentication getCurrentUser(Authentication authentication) {
        return authentication;
    }

    @GetMapping("/currentUsername")
    public String currentUserName(Principal principal) {
        return principal.getName();
    }

    /*
    ！！重要方式 1/2
     */
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName(); // 一般为 loginId
            return currentUserName;
        } else { // 解决匿名访问, 返回一个匿名用户的问题
            throw new RuntimeException("No User");
        }
    }
    /**
     * ！！重要方式 2/2
     * 很多时候我们自定义了用户对象UserDetails, 我们可以通过Spring Security 4.0提供的注解@AuthenticationPrincipal
     * 来获取当前用户的自定义UserDetails对象。如果CustomUser是UserDetails的实现，那么我们可以：
     *
     * @param customUser
     * @return
     */
    @GetMapping("/current_user")
    public UserDetails currentUserName(@AuthenticationPrincipal UserDetails customUser) {
        return customUser;
    }

    /**
     * simplify
     *
     * @param username
     * @return
     */
    @GetMapping("/current_user_name")
    public String currentUserName(
            @AuthenticationPrincipal(expression = "username") String username) {
        return username;
    }

    /**
     * Spring Security 5 提供了一个新的注解@CurrentSecurityContext来获取当前用户的安全上下文
     *
     * @param authentication
     * @return
     */
    @GetMapping("/current__username")
    public String currentUserName(
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        return authentication.getName();
    }
    /**
     * Spring Security 5 提供了一个新的注解@CurrentSecurityContext来获取当前用户的安全上下文
     *
     * @param securityContext
     * @return
     */
    @GetMapping("/current__username1")
    public String currentUserName1(
            @CurrentSecurityContext SecurityContext securityContext) {
        Authentication authentication = securityContext.getAuthentication();
        return authentication.getName();
    }

    /**
     * simplify
     *
     * <p>通过expression参数声明SpEL表达式来获取其它属性，例如获取Principal对象
     *
     * @param principal
     * @return
     */
    @GetMapping("/principal")
    public String getPrincipal(
            @CurrentSecurityContext(expression = "authentication.principal") Principal principal) {
        return principal.getName(); // null pointer
    }
}
