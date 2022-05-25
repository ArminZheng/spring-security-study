package com.armin.security.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.io.BufferedReader;
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
            HttpServletRequest quest, HttpServletResponse sponse) throws AuthenticationException {
        /*
        1 判断是否 post 方式请求
        2 判断是否 json 格式请求类型
        3 从 json 数据中获取用户输入用户名密码进行验证
         */
        if (!HttpMethod.POST.name().equals(quest.getMethod())) {
            throw new AuthenticationServiceException("method not support: " + quest.getMethod());
        }
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(quest.getContentType())) {
            try {
                BufferedReader reader = quest.getReader();
                Map<String, String> userInfo =
                        new Gson().fromJson(
                                reader, new TypeToken<Map<String, String>>() {}.getType());
                String username = userInfo.get(getUsernameParameter());
                String password = userInfo.get(getPasswordParameter());
                log.info("username = " + username + " | password = " + password);
                final UsernamePasswordAuthenticationToken authRequest =
                        new UsernamePasswordAuthenticationToken(username, password);
                setDetails(quest, authRequest);
                return this.getAuthenticationManager().authenticate(authRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.attemptAuthentication(quest, sponse);
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
