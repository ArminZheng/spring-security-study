package com.eastcom.ecfc.service;

/**
 * DService
 *
 * @author zy
 * @version 2022/5/9
 */
public interface DService extends BService, CService{
    @Override
    default String get(String s) {
        return BService.super.get(s);
    }
}
