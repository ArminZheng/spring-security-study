package com.armin.security.config;

import com.armin.security.security.filter.LoginKaptchaFilter;
import com.armin.security.service.LabelFilter;
import com.armin.security.service.MyUserDetailService;
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
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        // session 会话管理
        http.sessionManagement() // 开启会话管理
                .maximumSessions(1) // 最大同时登陆数
                // .and().invalidSessionUrl("invalidSession") // 错误session处理
                // 属于SessionManagementFilter重定向
                // .expiredUrl("/expired") // 被挤下线的会话过期处理 属于ConcurrentSessionFilter将重定向到expiredUrl
                .expiredSessionStrategy( // 过期策略（前后端分离）
                        event -> {
                            HttpServletResponse response = event.getResponse();
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            String json =
                                    "{\"success\":false,\"message\":\"SESSION_INVALID\",\"code\":401}";
                            response.getWriter().println(json);
                        })
                .maxSessionsPreventsLogin(true) // 登陆后禁止再次登陆(不允许二次登陆)
                // session 共享
                .sessionRegistry(sessionRegistry())
        ;
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

    private final LabelFilter labelFilter;

    private final FindByIndexNameSessionRepository sessionRepository;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry(sessionRepository);
    }

    /*
    旧版本使用 session 监听器的 maximumSessions 必须添加 HttpSessionEventPublisher
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
    */

    @Bean
    public RegistrationBean ssoFilter(LabelFilter filter) {
        FilterRegistrationBean<LabelFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
