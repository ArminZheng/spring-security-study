package com.armin.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * SecurityApp
 *
 * @author zy
 * @version 2022/5/5
 */
@SpringBootApplication
@ServletComponentScan // 启用 @WebServlet @WebFilter @WebListener
public class SecurityApp {
    // 过滤器被 @WebFilter 修饰后，就只会被包装为 FilterRegistrationBean
    public static void main(String[] args) {
        SpringApplication.run(SecurityApp.class, args);
    }
}
