package com.armin.security.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Role：主键（为了维护不能将主键等同于编码）、编码、名称、描述
 *
 * @author zy
 * @version 2022/5/16
 */
@Data
public class Role implements Serializable {
    private String id;
    private String code;
    private String name;
    private String description;
}
