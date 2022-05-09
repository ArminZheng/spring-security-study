package com.eastcom.ecfc.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * BService
 *
 * @author zy
 * @version 2022/5/9
 */
@Service
@Data
public class BService implements Serializable {
    String name = "hello wonm";
}
