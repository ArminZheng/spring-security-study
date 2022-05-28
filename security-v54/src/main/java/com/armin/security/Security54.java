package com.armin.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Security54
 *
 * @author zy
 * @version 2022/5/26
 */
@SpringBootApplication
@EnableCaching // 必须手动开启
public class Security54 {

    public static void main(String[] args) {
        SpringApplication.run(Security54.class, args);
    }
}
