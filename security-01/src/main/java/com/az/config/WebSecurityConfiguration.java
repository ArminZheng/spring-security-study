package com.az.config;

import com.az.security.filter.LoginKaptchaFilter;
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

/**
 * WebSecurityConfiguration
 *
 * @author zy
 * @version 2022/5/16
 */
@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    public UserDetailsService userDetailsService() {
        final InMemoryUserDetailsManager inMemoryUserDetailsManager =
                new InMemoryUserDetailsManager();
        inMemoryUserDetailsManager.createUser(
                User.withUsername("root").password("{noop}123").roles("admin").build());
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

    @Bean
    public LoginKaptchaFilter loginKaptchaFilter() throws Exception {
        final LoginKaptchaFilter loginKaptchaFilter = new LoginKaptchaFilter();
        loginKaptchaFilter.setFilterProcessesUrl("/doLogin");
        loginKaptchaFilter.setUsernameParameter("uname");
        loginKaptchaFilter.setPasswordParameter("passwd");
        loginKaptchaFilter.setKaptchaParameter("kaptcha");
        // 指定认证管理器
        loginKaptchaFilter.setAuthenticationManager(authenticationManagerBean());
        // 指定成功时处理
        // 指定失败时处理
        return loginKaptchaFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // authorizeRequests formLogin logout csrf
        http.authorizeRequests()
                .mvcMatchers("/v1/info/vc.png")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .and()
                .logout()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(
                        (req, res, ex) -> {
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.getWriter().println("请认证后访问！");
                        })
                .and()
                .csrf()
                .disable();
        http.addFilterAt(loginKaptchaFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
