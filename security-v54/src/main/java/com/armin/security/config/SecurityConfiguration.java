package com.armin.security.config;

import com.armin.security.security.filter.LoginFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * https://felord.cn/captchaAuthenticationFilter.html
 *
 * <p>https://felord.cn/authenticationConfiguration.html
 *
 * @author zy
 * @version 2022/5/26
 */
@Slf4j
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.antMatcher("/**") // 不同的 SecurityFilterChain 是互斥而且平等的，它们之间不是上下游关系。
                .authorizeRequests(
                        authorize ->
                                authorize
                                        .antMatchers("/withdraw")
                                        .hasAnyRole("ADMIN", "ACCOUNTANT")
                                        .anyRequest()
                                        .authenticated());
        http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);
        http.sessionManagement(
                session ->
                        session.maximumSessions(1)
                                .maxSessionsPreventsLogin(true)
                                .expiredSessionStrategy(
                                        event -> {
                                            HttpServletResponse response = event.getResponse();
                                            response.setContentType(
                                                    MediaType.APPLICATION_JSON_VALUE);
                                            String json =
                                                    "{\"success\":false,\"message\":\"SESSION_INVALID\",\"code\":401}";
                                            response.getWriter().println(json);
                                        }));
        // .sessionRegistry(sessionRegistry()) // session 共享
        http.csrf(
                csrf ->
                        csrf.csrfTokenRepository(
                                CookieCsrfTokenRepository
                                        .withHttpOnlyFalse())); // 将令牌保存到cookie中（并允许前端获取）
        http.authenticationManager(new CustomAuthenticationManager());
        return http.build();
    }

    @Bean
    public LoginFilter loginFilter() throws Exception {
        final LoginFilter loginFilter = new LoginFilter();
        loginFilter.setFilterProcessesUrl("/doLogin");
        loginFilter.setUsernameParameter("uname");
        loginFilter.setPasswordParameter("passwd");
        // 指定认证管理器
        loginFilter.setAuthenticationManager(authenticationManagerBean());
        // 指定成功时处理
        loginFilter.setAuthenticationSuccessHandler(
                (request, response, authentication) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.OK.value());
                    Map<String, Object> data = new HashMap<>();
                    data.put("userInfo", authentication.getPrincipal());
                    data.put("msg", "登陆成功");
                    PrintWriter out = response.getWriter();
                    ObjectMapper objectMapper = new ObjectMapper();
                    out.write(objectMapper.writeValueAsString(data));
                    out.flush();
                    out.close();
                });
        // 指定失败时处理
        loginFilter.setAuthenticationFailureHandler(
                (request, response, exception) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    Map<String, Object> data = new HashMap<>();
                    data.put("msg", "登陆失败：" + exception.getMessage());
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    PrintWriter out = response.getWriter();
                    ObjectMapper objectMapper = new ObjectMapper();
                    out.write(objectMapper.writeValueAsString(data));
                    out.flush();
                    out.close();
                });
        return loginFilter;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 仅仅作为演示
        return (web) -> web.ignoring().antMatchers("/withdraw/info", "/withdraw/hello");
    }
}
