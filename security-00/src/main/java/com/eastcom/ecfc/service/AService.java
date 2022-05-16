package com.eastcom.ecfc.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * AServicer
 *
 * @author zy
 * @version 2022/5/9
 */
@Service
@Data
public class AService implements Serializable {
    @Autowired
    BService bService;
    @Autowired
    CService cService;
}
