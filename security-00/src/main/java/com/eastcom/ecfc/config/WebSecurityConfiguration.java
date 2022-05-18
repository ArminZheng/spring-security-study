package com.eastcom.ecfc.config;

import com.eastcom.ecfc.security.KaptchaFilter;
import com.eastcom.ecfc.security.LoginFailureHandler;
import com.eastcom.ecfc.security.LoginFilter;
import com.eastcom.ecfc.security.LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSecurityConfigure
 *
 * @author zy
 * @version 2022/5/5
 */
@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(
                User.withUsername("luck").password("111").roles("admin").build());
        return userDetailsManager;
    }

    /**
     * 替换默认的 AuthenticationManagerBuilder
     *
     * <blockquote>
     *
     * <pre>
     * // @Autowired
     * public void initialize(AuthenticationManagerBuilder builder) throws Exception {
     *      // security 默认的 Builder
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param builder AuthenticationManagerBuilder
     * @throws Exception @exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
        // 自定义 Manager 并没有暴露出去
        String password = passwordEncoder().encode("123456");
        builder.inMemoryAuthentication().withUser("lucy").password(password).roles("admin")
        // .and().passwordEncoder(passwordEncoder())
        ;
        // auth.userDetailsService(new
        // CachingUserDetailsService()).passwordEncoder(passwordEncoder());
        builder.userDetailsService(userDetailsService());
    }

    /**
     * 将 AuthenticationManager 放入容器
     *
     * @return AuthenticationManager
     * @throws Exception @exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/login.html")
                .permitAll()
                .mvcMatchers("/vc.png")
                .permitAll()
                .mvcMatchers("/logout")
                .permitAll()
                .mvcMatchers("/index")
                .permitAll()
                // 放行资源要写在【任何】前面
                .anyRequest()
                .authenticated()
                .and() // 匿名内部类中使用: 类名.this.属性名,调用外部类属性 e.g.
                // ExpressionUrlAuthorizationConfigurer.this.and();
                .formLogin()
                // 登陆操作 在 formLogin 后面，对登陆进行个性化设置
                .loginPage("/login.html") // 指定登陆页面，一旦定义必须指定登陆api
                .loginProcessingUrl("/doLogin") // 指定登陆api ，必须同时指定登陆页面
                .usernameParameter("uname") // 修改默认用户名参数
                .passwordParameter("pwd") // 修改默认密码参数
                // .successForwardUrl("/hello") // forward 转发，url不变 （只能二选一）
                // .defaultSuccessUrl("/hello") // redirect 重定向，url改变（只能二选一）
                // default是指如果之前有历史访问，则直接跳转历史，就不去成功页面了
                // .defaultSuccessUrl("/index", true) // default的特性，如果之前访问受限资源，会优先上一次。需要设为true才能强转
                .successHandler(new LoginSuccessHandler())
                // .failureForwardUrl("/login.html") // 转发
                // {request} 作用域中拿
                // .failureUrl("/login.html") // 重定向（sendRedirect)
                // {session} 作用域中拿
                .failureHandler(new LoginFailureHandler())
                .and()
                // 登出操作 在 HttpSecurity 类中, 前面需要加 and()
                .logout()
                // .logoutUrl("/logout") // default true
                .logoutRequestMatcher(
                        new OrRequestMatcher(
                                new AntPathRequestMatcher("/aa", HttpMethod.GET.name()),
                                new AntPathRequestMatcher("/bb", HttpMethod.POST.name())))
                .invalidateHttpSession(true) // default true
                .clearAuthentication(true) // default true
                // .logoutSuccessUrl("/login.html")
                .logoutSuccessHandler(
                        (req, res, auth) -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("msg", "注销成功");
                            data.put("用户信息", auth.getPrincipal());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            res.setStatus(HttpStatus.OK.value());
                            res.getWriter().println(new ObjectMapper().writeValueAsString(data));
                        })
                .and()
                // 异常处理
                .exceptionHandling()
                // 异常处理 -> 认证异常
                .authenticationEntryPoint(
                        (req, res, auth) -> {
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.getWriter().println("请认证后处理！");
                        })
                // 异常处理 -> 授权异常
                .accessDeniedHandler(
                        (req, res, auth) -> {
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().println("权限不足，请联系管理员!");
                        })

                // 以后再说
                .and()
                .csrf()
                .disable(); // 禁止 csrf 跨站请求保护
        /*
        at 替换
        before 之前
        after 之后
        */
        http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(kaptchaFilter(), UsernamePasswordAuthenticationFilter.class);
        // begin 未知知识
        /*http.sessionManagement().invalidSessionUrl("/sessionInvalid");
        http.sessionManagement().maximumSessions(1);
        http.authorizeRequests().withObjectPostProcessor(
                new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                        // object.setSecurityMetadataSource(
                        //         urlFilterInvocationSecurityMetadataSource);
                        // object.setAccessDecisionManager(urlAccessDecisionManager);
                        return object;
                    }
                });*/
    }

    /*@Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                // 主页 静态资源白名单
                .antMatchers("/index.html").antMatchers("/user/isUserLoggedIn").antMatchers("/user/getCurrentUserInfo")
                .antMatchers("/notFound").antMatchers("/error").antMatchers("/static/**")
                .antMatchers("/user/distroySession")//登出
                .antMatchers("/sessionInvalid")//sesseion过期
                // swagger 接口白名单
                .antMatchers("/swagger-ui.html/**").antMatchers("/webjars/**").antMatchers("/v2/**")
                .antMatchers("/swagger-resources/**").antMatchers("/pushWebsocket/**")
                .antMatchers("/agent/**")
                .antMatchers("/termReq/**")
                .antMatchers("/code/image")
                .antMatchers("/test/**");
    }*/
    // end 未知知识
    @Bean
    public LoginFilter loginFilter() throws Exception {
        LoginFilter loginFilter = new LoginFilter();
        loginFilter.setUsernameParameter("uname");
        loginFilter.setPasswordParameter("pwd");
        // note: builder bind userDetailsService()
        loginFilter.setAuthenticationManager(authenticationManagerBean());
        return loginFilter;
    }

    @Bean
    public KaptchaFilter kaptchaFilter() throws Exception {
        KaptchaFilter kaptchaFilter = new KaptchaFilter();
        kaptchaFilter.setAuthenticationManager(authenticationManagerBean());
        return kaptchaFilter;
    }
}
