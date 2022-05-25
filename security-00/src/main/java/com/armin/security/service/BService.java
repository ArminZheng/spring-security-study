package com.armin.security.service;

import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * BService
 *
 * @author zy
 * @version 2022/5/9
 */
@Service
public interface BService extends Serializable {
    String name = "hello wonm";

    default String get(String s) {
        return "BS";
    }
}
