package com.eastcom.ecfc.config;

import com.eastcom.ecfc.security.filter.LoginKaptchaFilter;
import com.eastcom.ecfc.service.LabelFilter;
import com.eastcom.ecfc.service.MyUserDetailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.RegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        loginKaptchaFilter.setRememberMeServices(
                rememberMeServices()); // 8 前后端分离后，更改了获取方式后，存放也需要同步设置(2/2)
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
        http.authorizeRequests() // 5 authorizeHttpRequests 没有办法使用 mvcMatchers.rememberMe
                .mvcMatchers("/v1/info/vc.png")
                .permitAll()
                // .mvcMatchers("/index.html").rememberMe() // 6 指定资源使用 rememberMe
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .and()
                .rememberMe() // 1 开启 RememberMe 功能
                .rememberMeServices(rememberMeServices()) // 8 前后端分离后，更改了获取方式后，存放也需要同步设置(1/2)
                // .tokenRepository(persistentTokenRepository()) // 7 使用数据库 RememberMe (方式二)
                // .alwaysRemember(true) // 3 总是使用 RememberMe 功能
                // .rememberMeParameter("rememberMe") // 2 更改默认接收参数
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
        http.addFilterAfter(labelFilter, UsernamePasswordAuthenticationFilter.class);
        // http.addFilterBefore(testFilter, UsernamePasswordAuthenticationFilter.class);
    }

    // private DataSource dataSource;

    /* 4 remember me service */
    @Bean
    public RememberMeServices rememberMeServices() {
        // 7 使用数据库 (方式一)
        // JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        // jdbcTokenRepository.setDataSource(dataSource);
        // 得自己创建表

        InMemoryTokenRepositoryImpl inMemoryTokenRepository = new InMemoryTokenRepositoryImpl();
        // 5 使用这种方式，在 session 过期后，就会使用 cookie:rememberMe, 任意一次使用都会对 值进行更新
        return new PersistentTokenBasedRememberMeServices(
                UUID.randomUUID().toString(), myUserDetailService, inMemoryTokenRepository) {
            /** 8 前后端分离后 需要重写获取 remember-me 开启条件值的方式，比如 json 形式就必须从请求体里面获取 */
            @Override
            protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
                String paramValue = request.getParameter(parameter);
                if (paramValue != null
                        && (paramValue.equalsIgnoreCase("true")
                                || paramValue.equalsIgnoreCase("on")
                                || paramValue.equalsIgnoreCase("yes")
                                || paramValue.equals("1"))) {
                    return true;
                }
                return false;
            }
        };
    }

    // 7 使用数据库 RememberMe (方式二)
    // @Bean
    // public PersistentTokenRepository persistentTokenRepository() {
    //     JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
    //     jdbcTokenRepository.setDataSource(dataSource); // 得引入 jdbc
    //     jdbcTokenRepository.setCreateTableOnStartup(true); // 启动时创建表结构
    //     return jdbcTokenRepository;
    // }

    private LabelFilter labelFilter;

    @Bean
    public RegistrationBean ssoFilter(LabelFilter filter) {
        FilterRegistrationBean<LabelFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
