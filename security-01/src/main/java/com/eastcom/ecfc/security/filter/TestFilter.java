package com.eastcom.ecfc.security.filter;

import com.eastcom.ecfc.domain.User;
import com.eastcom.ecfc.service.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TestFilter
 *
 * @author zy
 * @version 2022/5/18
 */
@Component
@AllArgsConstructor
public class TestFilter extends OncePerRequestFilter {

    UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String parameter = request.getParameter("token");
        if (parameter != null){
            User user = userMapper.loadUserByUsername("lisi");
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
