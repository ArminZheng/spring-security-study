package com.armin.security.config;

import cn.hutool.core.util.RandomUtil;
import com.armin.security.service.CaptchaCacheStorage;
import com.armin.security.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * CaptchaAuthenticationConfiguration
 *
 * @author zy
 * @version 2022/5/27
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class CaptchaAuthenticationConfiguration {
    private static final String SMS_CAPTCHA_CACHE = "captcha";

    /**
     * spring cache 管理验证码的生命周期.
     *
     * @return the captcha cache storage
     */
    @Bean
    CaptchaCacheStorage captchaCacheStorage() {
        return new CaptchaCacheStorage() {
            @CachePut(cacheNames = SMS_CAPTCHA_CACHE, key = "#phone")
            @Override
            public String put(String phone) {
                return RandomUtil.randomNumbers(5);
            }

            @Cacheable(cacheNames = SMS_CAPTCHA_CACHE, key = "#phone")
            @Override
            public String get(String phone) {
                return null;
            }

            @CacheEvict(cacheNames = SMS_CAPTCHA_CACHE, key = "#phone")
            @Override
            public void expire(String phone) {}
        };
    }

    /**
     * 验证码服务. 两个功能： 发送和校验.
     *
     * @param captchaCacheStorage the captcha cache storage
     * @return the captcha service
     */
    @Bean
    public CaptchaService captchaService(CaptchaCacheStorage captchaCacheStorage) {
        return new CaptchaService() {
            @Override
            public boolean sendCaptcha(String phone) {
                String existed = captchaCacheStorage.get(phone);
                if (StringUtils.hasText(existed)) {
                    // 节约成本的话如果缓存存在可用的验证码 不再发新的验证码
                    log.warn("captcha code 【 {} 】 is available now", existed);
                    return false;
                }
                // 生成验证码并放入缓存
                String captchaCode = captchaCacheStorage.put(phone);
                log.info("captcha: {}", captchaCode);

                // todo 这里自行完善调用第三方短信服务 (告知用户验证码，开发时可在 Redis 中手动查看)
                return true;
            }

            @Override
            public boolean verifyCaptcha(String phone, String code) {
                String cacheCode = captchaCacheStorage.get(phone);

                if (Objects.equals(cacheCode, code)) {
                    // 验证通过手动过期
                    captchaCacheStorage.expire(phone);
                    return true;
                }
                return false;
            }
        };
    }
}
