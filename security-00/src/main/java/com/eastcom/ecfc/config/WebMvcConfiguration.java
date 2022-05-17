package com.eastcom.ecfc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 对 Spring MVC 进行自定义配置
 *
 * @author zy
 * @version 2022/5/10
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    /**
     * 为了搭配 thymeleaf 使用
     * @param registry 视图自动化注册器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("login.html").setViewName("login");
        registry.addViewController("logout.html").setViewName("logout");
    }
}
