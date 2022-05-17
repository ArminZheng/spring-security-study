package com.az.service;

import com.az.domain.User;
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
    // User 继承 UserDetails UserServer 实现 UserDetailsService，为了能更新密码还需实现 UserDetailsPasswordService
    private UserMapper userMapper;

    // 取回用户的同时还需加载【角色信息】
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
            ((User) user).setPassword(newPassword);
        }
        return user;
    }
}
