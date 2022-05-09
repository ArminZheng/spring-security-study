package com.eastcom.ecfc.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * XyzService
 *
 * @author zy
 * @version 2022/5/9
 */
@Service
@Data
public class XyzService implements DService, Serializable {
    String name = "zhangsan";
    Integer age = 18;

    public void toBig() {
        System.out.println("name = " + name + " is big boy, age " + age);
    }
}
