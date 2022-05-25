package com.armin.security.config;

import com.armin.security.security.KaptchaFilter;
import com.armin.security.security.LoginFailureHandler;
import com.armin.security.security.LoginFilter;
import com.armin.security.security.LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.debug.DebugFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        http.sessionManagement()
                .invalidSessionUrl("/sessionInvalid"); // session 非法处理url（全局处理，且自动放开访问）
        http.sessionManagement().maximumSessions(1);
        // begin 未知知识
        http.authorizeRequests()
                .withObjectPostProcessor(
                        new ObjectPostProcessor<FilterSecurityInterceptor>() {
                            @Override
                            public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                                // object.setSecurityMetadataSource(
                                //         urlFilterInvocationSecurityMetadataSource);
                                // object.setAccessDecisionManager(urlAccessDecisionManager);
                                return object;
                            }
                        });
    }

    /**
     * WebSecurity 用于配置全局的某些通用事物，例如静态资源等
     *
     * <p>放行所有免认证就可以访问的路径，比如注册业务，登录业务，退出业务，静态资源等
     *
     * <p>HttpSecurity 可以同时配置角色, 当使用 permitAll() 时。请求将被允许从 Security Filter Chain 访问, 这是昂贵的
     *
     * <p>authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated();
     *
     * <p>WebSecurity 方便放行, 适合不需要任何 身份验证/授权 即可查看或读取的图像、javascript 文件
     *
     * <p>ignoring().antMatchers("/admin/**", "/public/**"); // 这时上面的 /admin 也会失效
     *
     * @param web WebSecurity
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                // 主页 静态资源白名单
                .antMatchers("/index.html")
                .antMatchers("/user/isUserLoggedIn")
                .antMatchers("/user/getCurrentUserInfo")
                .antMatchers("/notFound")
                .antMatchers("/error")
                .antMatchers("/static/**")
                .antMatchers("/user/distroySession") // 登出
                .antMatchers("/sessionInvalid") // sesseion过期
                // swagger 接口白名单
                .antMatchers("/swagger-ui.html/**")
                .antMatchers("/webjars/**")
                .antMatchers("/v2/**")
                .antMatchers("/swagger-resources/**")
                .antMatchers("/pushWebsocket/**")
                .antMatchers("/agent/**")
                .antMatchers("/termReq/**")
                .antMatchers("/code/image")
                .antMatchers("/test/**");
    }

    /**
     * In order to finely manage the life cycle of multiple SecurityFilterChain, it is necessary to
     * have a unified management agent for these SecurityFilterChain, which is the meaning of
     * WebSecurity. Here is the underlying logic of the build method of WebSecurity.
     *
     * <p>为了精细地管理多个生命周期SecurityFilterChain，就需要对这些有一个统一的管理代理SecurityFilterChain，这就是WebSecurity.
     * 这里是build方法的底层逻辑WebSecurity。
     *
     * <p>As you can see from the source code above, WebSecurity is used to build a Spring bean
     * FilterChainProxy called springSecurityFilterChain. Its role is to define those requests that
     * ignore security controls and those that must, clearing SecurityContext when appropriate to
     * avoid memory leaks, and also to define request firewalls and request rejection processors,
     * plus we turn on Spring Security Debug mode which is also configured here.
     *
     * <p>我们事实上可以认为，WebSecurity是Spring Security对外的唯一出口，而HttpSecurity只是内部安全策略的定义方式；
     * WebSecurity对标FilterChainProxy，而HttpSecurity则对标SecurityFilterChain，另外它们的父类都是AbstractConfiguredSecurityBuilder。
     *
     * @return
     * @throws Exception
     */
    protected Filter performBuild() throws Exception {
        Assert.state(
                !this.securityFilterChainBuilders.isEmpty(),
                () ->
                        "At least one SecurityBuilder<? extends SecurityFilterChain> needs to be specified. "
                                + "Typically this is done by exposing a SecurityFilterChain bean "
                                + "or by adding a @Configuration that extends WebSecurityConfigurerAdapter. "
                                + "More advanced users can invoke "
                                + WebSecurity.class.getSimpleName()
                                + ".addSecurityFilterChainBuilder directly");
        // 被忽略请求的个数 和 httpscurity的个数 构成了过滤器链集合的大小
        int chainSize = this.ignoredRequests.size() + this.securityFilterChainBuilders.size();
        List<SecurityFilterChain> securityFilterChains = new ArrayList<>(chainSize);
        // 初始化过滤器链集合中的 忽略请求过滤器链
        for (RequestMatcher ignoredRequest : this.ignoredRequests) {
            securityFilterChains.add(new DefaultSecurityFilterChain(ignoredRequest));
        }
        // 初始化过滤器链集合中的 httpsecurity 定义的过滤器链
        for (SecurityBuilder<? extends SecurityFilterChain> securityFilterChainBuilder :
                this.securityFilterChainBuilders) {
            securityFilterChains.add(securityFilterChainBuilder.build());
        }
        FilterChainProxy filterChainProxy = new FilterChainProxy(securityFilterChains);
        if (this.httpFirewall != null) {
            // 请求防火墙
            filterChainProxy.setFirewall(this.httpFirewall);
        }
        if (this.requestRejectedHandler != null) {
            // 请求拒绝处理器
            filterChainProxy.setRequestRejectedHandler(this.requestRejectedHandler);
        }
        filterChainProxy.afterPropertiesSet();

        Filter result = filterChainProxy;
        if (this.debugEnabled) {
            this.logger.warn(
                    "\n\n"
                            + "********************************************************************\n"
                            + "**********        Security debugging is enabled.       *************\n"
                            + "**********    This may include sensitive information.  *************\n"
                            + "**********      Do not use in a production system!     *************\n"
                            + "********************************************************************\n\n");
            result = new DebugFilter(filterChainProxy);
        }
        this.postBuildAction.run();
        return result;
    }
    /**
     * There is also a role that may not be mentioned in other articles, FilterChainProxy is the
     * only export of Spring Security to the Spring framework application, which is then combined
     * with a Servlet in Spring’s bridge proxy DelegatingFilterProxy. which constitutes Spring’s
     * only export to the Servlet system. This isolates Spring Security, Spring framework and
     * Servlet API. end
     */
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
