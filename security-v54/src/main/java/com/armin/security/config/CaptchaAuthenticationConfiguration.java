package com.armin.security.config;

import cn.hutool.core.util.RandomUtil;
import com.armin.security.security.CaptchaAuthenticationProvider;
import com.armin.security.security.filter.CaptchaAuthenticationFilter;
import com.armin.security.service.CaptchaCacheStorage;
import com.armin.security.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

import java.util.Collections;
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

                // todo 这里自行完善调用第三方短信服务
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

    /**
     * 自行实现根据手机号查询可用的用户，这里简单举例. 注意该接口可能出现多态。所以最好加上注解@Qualifier
     *
     * @return the user details service
     */
    @Bean
    @Qualifier("captchaUserDetailsService")
    public UserDetailsService captchaUserDetailsService() {
        // 验证码登陆后密码无意义了但是需要填充一下
        return username ->
                User.withUsername(username)
                        .password("TEMP")
                        // todo  这里权限 你需要自己注入
                        .authorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_APP"))
                        .build();
    }

    /**
     * 验证码认证器.
     *
     * @param captchaService the captcha service
     * @param userDetailsService the user details service
     * @return the captcha authentication provider
     */
    @Bean
    public CaptchaAuthenticationProvider captchaAuthenticationProvider(
            CaptchaService captchaService,
            @Qualifier("captchaUserDetailsService") UserDetailsService userDetailsService) {
        return new CaptchaAuthenticationProvider(userDetailsService, captchaService);
    }

    /**
     * 验证码认证过滤器.
     *
     * @param authenticationSuccessHandler the authentication success handler
     * @param authenticationFailureHandler the authentication failure handler
     * @param captchaAuthenticationProvider the captcha authentication provider
     * @return the captcha authentication filter
     */
    @Bean
    public CaptchaAuthenticationFilter captchaAuthenticationFilter(
            AuthenticationSuccessHandler authenticationSuccessHandler,
            AuthenticationFailureHandler authenticationFailureHandler,
            CaptchaAuthenticationProvider captchaAuthenticationProvider) {
        CaptchaAuthenticationFilter captchaAuthenticationFilter = new CaptchaAuthenticationFilter();
        // 配置 authenticationManager
        ProviderManager providerManager =
                new ProviderManager(Collections.singletonList(captchaAuthenticationProvider));
        captchaAuthenticationFilter.setAuthenticationManager(providerManager);
        // 成功处理器
        captchaAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        // 失败处理器
        captchaAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);

        return captchaAuthenticationFilter;
    }
}
