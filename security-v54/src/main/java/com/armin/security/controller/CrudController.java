package com.armin.security.controller;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Objects;

/**
 * WithdrawController
 *
 * @author zy
 * @version 2022/5/23
 */
@Slf4j
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
                    "错误提示码"
                            + Objects.requireNonNull(bindingResult.getFieldError())
                                    .getDefaultMessage());
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

    /**
     * 只需写上类名，就会自动帮我们注入
     *
     * @param authentication 登录关键参数的载体，亦称凭证
     * @param principal 主体身份 登陆成功才会填充
     * @return 登录的用户
     */
    @GetMapping("/currentUser")
    public Authentication getCurrentUser(Authentication authentication, Principal principal) {
        // Principal 代表登录的用户。Authentication 接口扩展了 Principal 接口，所以 Authentication is a Principal。
        // 在用户通过身份验证之前，Authentication 可以代表身份验证请求的令牌。
        // 用户通过身份验证后，它可以提供有关主体的额外信息，例如 getAuthorities(..)，在成功身份验证后，您可以获得主体已被授予的权限。
        log.info("current user is " + principal.getName());
        return authentication;
    }

    /**
     * ！！重要方式 1/2 提取出通用公共方法，可以写在抽象类里
     *
     * @return return
     */
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName(); // 一般为 loginId
        } else { // 解决匿名访问, 返回一个匿名用户的问题
            throw new RuntimeException("No User");
        }
    }
    /**
     * ！！重要方式 2/2 自定义了用户对象UserDetails Spring Security 4.0 提供了注解 @AuthenticationPrincipal
     * 来获取当前用户的自定义UserDetails对象。 如果 CustomUser 是 UserDetails 的实现，那么我们可以：CustomUser customUser
     *
     * @param customUser customUser
     * @return return
     */
    @GetMapping("/current_user")
    public UserDetails currentUserName(@AuthenticationPrincipal UserDetails customUser) {
        return customUser;
    }

    /**
     * simplify 方式 2 的简单版，crud 基本款
     *
     * @param username username
     * @return return
     */
    @GetMapping("/current_user_name")
    public String currentUserName(
            @AuthenticationPrincipal(expression = "username") String username) {
        return username;
    }

    /**
     * Spring Security 5 提供了一个新的注解 @CurrentSecurityContext 来获取当前用户的安全上下文
     *
     * @param securityContext 安全上下文
     * @return name
     */
    @GetMapping("/current_username")
    public String currentUserName1(@CurrentSecurityContext SecurityContext securityContext) {
        Authentication authentication = securityContext.getAuthentication();
        return authentication.getName();
    }

    /**
     * 用5的注解 @CurrentSecurityContext 来获取当前用户
     *
     * <pre>
     *     注意：
     *    // null pointer @CurrentSecurityContext(expression = "authentication.principal")
     * </pre>
     *
     * @param authentication 凭证
     * @return loginId
     */
    @GetMapping("/current__username")
    public String currentUserName(
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        return authentication.getName();
    }
}
