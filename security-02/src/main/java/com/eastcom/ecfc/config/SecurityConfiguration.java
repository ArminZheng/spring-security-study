package com.eastcom.ecfc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSecurityConfiguration
 *
 * @author zy
 * @version 2022/5/23
 */
@Configuration
@AllArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/withdraw/info")
                .permitAll()
                .mvcMatchers("/login.html")
                .permitAll()
                .anyRequest()
                .authenticated();

        http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);

        // .formLogin();
        http.sessionManagement()
                .maximumSessions(1)
                .expiredSessionStrategy(
                        event -> {
                            HttpServletResponse response = event.getResponse();
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            String json =
                                    "{\"success\":false,\"message\":\"SESSION_INVALID\",\"code\":401}";
                            response.getWriter().println(json);
                        })
        // .maxSessionsPreventsLogin(true)
        // .sessionRegistry(sessionRegistry())
        ;
        http.csrf()
                .csrfTokenRepository(
                        CookieCsrfTokenRepository.withHttpOnlyFalse()); // 将令牌保存到cookie中（并允许前端获取）
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
        inMemoryUserDetailsManager.createUser(
                User.withUsername("admin").password("{noop}1234").roles("ADMIN").build());
        return inMemoryUserDetailsManager;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /* 分布式 session
    private final FindByIndexNameSessionRepository<?> sessionRepository;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }
    */
}
