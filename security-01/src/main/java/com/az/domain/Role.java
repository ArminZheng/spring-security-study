package com.az.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Role
 *
 * @author zy
 * @version 2022/5/16
 */
@Data
public class Role implements Serializable {
    private String id;
    private String name;
}
