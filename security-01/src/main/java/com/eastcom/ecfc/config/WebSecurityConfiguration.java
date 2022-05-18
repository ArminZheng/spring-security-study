package com.eastcom.ecfc.config;

import com.eastcom.ecfc.security.filter.LoginKaptchaFilter;
import com.eastcom.ecfc.service.MyUserDetailService;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSecurityConfiguration
 *
 * @author zy
 * @version 2022/5/16
 */
@Configuration
@AllArgsConstructor
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private MyUserDetailService myUserDetailService;

    /**
     * 在这里注册 userDetailService 以替换下面两种方式：
     *
     * <p>
     *
     * <pre>
     *     builder.inMemoryAuthentication()
     *         .withUser("lucy").password(password).roles("admin")
     * </pre>
     *
     * 或
     *
     * <pre>
     *     ...
     *         auth.userDetailsService(userDetailsService());
     *     }
     *     ...
     *     &#064;Autowired
     *     public UserDetailsService userDetailsService() {
     *         final InMemoryUserDetailsManager inMemory =
     *                 new InMemoryUserDetailsManager();
     *         inMemory.createUser(User.withUsername("root")
     *                 .password("{noop}123").roles("admin").build());
     *         return inMemory;
     *     }
     * </pre>
     *
     * @param auth the {@link AuthenticationManagerBuilder} to use
     * @throws Exception exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailService);
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
        loginKaptchaFilter.setAuthenticationSuccessHandler(
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
        loginKaptchaFilter.setAuthenticationFailureHandler(
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
        return loginKaptchaFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // authorizeRequests formLogin logout csrf
        http.authorizeHttpRequests()
                .mvcMatchers("/v1/info/vc.png")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .and()
                .rememberMe() // 开启 RememberMe 功能
                // .alwaysRemember(true) // 总是使用 RememberMe 功能
                // .rememberMeParameter("rememberMe") // 更改默认接收参数
                .and()
                .logout()
                // .and()
                // .exceptionHandling()
                // .authenticationEntryPoint(
                //         (req, res, ex) -> {
                //             res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                //             res.setStatus(HttpStatus.UNAUTHORIZED.value());
                //             res.getWriter().println("请认证后访问！");
                //         })
                .and()
                .csrf()
                .disable();
        http.addFilterAt(loginKaptchaFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
