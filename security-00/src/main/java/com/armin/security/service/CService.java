package com.armin.security.service;

import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * CService
 *
 * @author zy
 * @version 2022/5/9
 */
@Service
public interface CService extends Serializable {

    String name = "world working";

    String get(String s);

}
