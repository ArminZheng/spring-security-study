package com.eastcom.ecfc.controller;

import com.eastcom.ecfc.aop.AspectEnable;
import com.eastcom.ecfc.service.AService;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * InfoController
 *
 * @author zy
 * @version 2022/5/6
 */
@RestController
public class InfoController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(InfoController.class);

    @Autowired AService service;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Resource(name = "redisTemplate")
    ValueOperations<String, String> valueOps;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Resource(name = "redisTemplate")
    ValueOperations<String, AService> AOps;
    @Autowired
    AspectEnable aspectEnable;

    @GetMapping("/info")
    public Object info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("身份信息：" + authentication.getPrincipal());
        log.info("权限信息：" + authentication.getAuthorities()); // role
        log.info("凭证信息：" + authentication.getCredentials()); // 受保护的，会被擦除
        new Thread(() -> {
            // -Dspring.security.strategy=MODE_INHERITABLETHREADLOCAL
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("身份信息：" + auth.getPrincipal());
        }, "t1").start();
        return authentication.getPrincipal(); // 主角、当事人
        // （中小学） headmaster; schoolmaster; principal;
        // （大专院校） president; chancellor
    }

    @RequestMapping("/haha")
    public void haha() {
        System.err.println(">>>>>>>>>>>>>> test void");
    }

    @GetMapping("/redis/{key}/{value}")
    public String redis(@PathVariable("key") String key, @PathVariable("value") String value) {
        valueOps.set(key, value);
        log.info("set 成功");
        return value;
    }

    @GetMapping("/redis/{key}")
    public String redis(@PathVariable("key") String key) {
        String result = valueOps.get(key);
        log.info(result);
        System.out.println("aspectEnable.toString() = " + aspectEnable.toString());
        return result;
    }

    @GetMapping("/test1")
    public String test1() {
        AOps.set("a_service", service, 5, TimeUnit.MINUTES);
        log.info(service.toString());
        return "success";
    }

    @GetMapping("/test2")
    public AService test2() {
        AService a_service = AOps.get("a_service");
        log.info(a_service.toString());
        return a_service;
    }
}
