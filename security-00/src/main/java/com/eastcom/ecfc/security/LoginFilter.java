package com.eastcom.ecfc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * LoginFilter
 *
 * @author zy
 * @version 2022/5/10
 */
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        /*
        1 判断是否 post 方式请求
        2 判断是否 json 格式请求类型
        3 从 json 数据中获取用户输入用户名密码进行验证
         */
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationServiceException("method not support: " + request.getMethod());
        }
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(request.getContentType())) {
            try {
                Map<String, String> userInfo =
                        new ObjectMapper().readValue(request.getInputStream(), Map.class);
                String username = userInfo.get(getUsernameParameter());
                String password = userInfo.get(getPasswordParameter());
                log.info("username = " + username + " | password = " + password);
                final UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
                setDetails(request, authRequest);
                return this.getAuthenticationManager().authenticate(authRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.attemptAuthentication(request, response);
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return super.obtainPassword(request);
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return super.obtainUsername(request);
    }
}
