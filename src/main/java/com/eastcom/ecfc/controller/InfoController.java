package com.eastcom.ecfc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * InfoController
 *
 * @author zy
 * @version 2022/5/6
 */
@RestController
@Slf4j
public class InfoController {

    @GetMapping("/info")
    public Object info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("身份信息：" + authentication.getPrincipal());
        log.info("权限信息：" + authentication.getAuthorities()); // role
        log.info("凭证信息：" + authentication.getCredentials()); // 受保护的，会被擦除
        new Thread(()->{
            // -Dspring.security.strategy=MODE_INHERITABLETHREADLOCAL
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("身份信息：" + auth.getPrincipal());
        }, "t1").start();
        return authentication.getPrincipal(); // 主角、当事人
        // （中小学） headmaster; schoolmaster; principal;
        // （大专院校） president; chancellor
    }
}
