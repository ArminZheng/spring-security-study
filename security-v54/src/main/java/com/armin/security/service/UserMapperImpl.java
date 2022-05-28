package com.armin.security.service;

import com.armin.security.domain.Role;
import com.armin.security.domain.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * UserMapperImpl
 *
 * @author zy
 * @version 2022/5/16
 */
@Service
public class UserMapperImpl implements UserMapper {
    @Override
    public User loadUserByUsername(String username) {
        return USERS.get(username);
    }

    @Override
    public List<Role> getRolesById(String id) {
        return new ArrayList<>();
    }

    @Override
    public int updatePassword(String username, String newPassword) {
        User user = USERS.get(username);
        user.setPassword(newPassword);
        USERS.put(username, user);
        return 1;
    }
}
