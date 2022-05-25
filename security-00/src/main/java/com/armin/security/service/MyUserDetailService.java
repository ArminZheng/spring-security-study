package com.armin.security.service;

import com.armin.security.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * MyUserDetailService
 *
 * @author zy
 * @version 2022/5/16
 */
@Service
@AllArgsConstructor
public class MyUserDetailService implements UserDetailsService, UserDetailsPasswordService {

    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.loadUserByUsername(username);
        user.setRoles(userMapper.getRolesById(user.getId()));
        return user;
    }

    @Override // 默认使用 DelegatingPasswordEncoder 默认使用的是相当安全的加密方式
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        int result = userMapper.updatePassword(user.getUsername(), newPassword);
        if (result == 1) {
            ((User)user).setPassword(newPassword);
        }
        return user;
    }
}
