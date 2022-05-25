package com.armin.security.service;


import com.armin.security.domain.Role;
import com.armin.security.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserMapper
 *
 * @author zy
 * @version 2022/5/16
 */
public interface UserMapper {

    Map<String, User> USERS =
            new HashMap<String, User>() {
                {
                    put("zhangsan", new User("1", "zhangsan", "{noop}123"));
                    put("lisi", new User("2", "lisi", "{noop}123"));
                    put("wangwu", new User("3", "wangwu", "{noop}123"));
                }
            };

    default User loadUserByUsername(String username) {
        return USERS.get(username);
    }

    default List<Role> getRolesById(String id) {
        return new ArrayList<>();
    }

    default int updatePassword(String username, String newPassword) {
        User user = USERS.get(username);
        user.setPassword(newPassword);
        USERS.put(username, user);
        return 1;
    }
}
