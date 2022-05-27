package com.armin.security.controller;

import com.armin.security.domain.Result;
import com.armin.security.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    CaptchaService captchaService;

    /**
     * 模拟手机号发送验证码.
     *
     * @param phone the mobile
     * @return the rest
     */
    @GetMapping("/{phone}")
    public Result<?> captchaByMobile(@PathVariable String phone) {
        // todo 手机号 正则验证
        log.info(">>> input phone number: " + phone);
        if (captchaService.sendCaptcha(phone)) {
            return Result.ok("验证码发送成功");
        }
        return Result.error(-999, "验证码发送失败");
    }
}
