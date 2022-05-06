package com.eastcom.ecfc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSecurityConfigure
 *
 * @author zy
 * @version 2022/5/5
 */
@Configuration
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/login.html").permitAll()
                .mvcMatchers("/login").permitAll()
                .mvcMatchers("/logout").permitAll()
                .mvcMatchers("/index")
                .permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                // .loginPage("/login.html")
                .usernameParameter("uname")
                .passwordParameter("pwd")
                .successHandler((request, response, authentication) -> {
                    Object principal = null;
                    if (authentication != null) {
                        principal = authentication.getPrincipal();
                    }
                    response.setContentType("application/json;charset=utf-8");

                    request.getSession().setAttribute("userInfo", principal);
                    final Map<String, Object> map = new HashMap<>();
                    map.put("userInfo", principal);
                    final PrintWriter writer = response.getWriter();
                    writer.write(new ObjectMapper().writeValueAsString(map));
                    writer.flush();
                    writer.close();
                });
    }
}
