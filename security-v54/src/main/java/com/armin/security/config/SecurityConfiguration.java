package com.armin.security.config;

import com.armin.security.security.CaptchaAuthenticationProvider;
import com.armin.security.security.filter.CaptchaAuthenticationFilter;
import com.armin.security.service.CaptchaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collections;
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
@AllArgsConstructor
public class SecurityConfiguration {

    final CaptchaService captchaService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.antMatcher("/**") // 不同的 SecurityFilterChain 是互斥而且平等的，它们之间不是上下游关系。可通过 /api 区分
                .authorizeRequests(
                        authorize ->
                                authorize
                                        // .antMatchers("/withdraw")
                                        // .permitAll()
                                        // .hasAnyRole("ADMIN", "ACCOUNTANT")
                                        .anyRequest()
                                        .authenticated());
        // http.formLogin().and().logout();
        // http.rememberMe()
        //         .rememberMeServices(null)
        //         .tokenRepository(new JdbcTokenRepositoryImpl(){{
        //             this.setDataSource(dataSource); // 得引入 jdbc
        //             setCreateTableOnStartup(true); // 启动时创建表结构
        //         }})
        //         .alwaysRemember(true)
        //         .rememberMeParameter("rememberMe");
        http.exceptionHandling(
                exception ->
                        exception
                                .authenticationEntryPoint(
                                        (req, res, auth) -> {
                                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                                            res.getWriter().println("请认证后处理！");
                                        })
                                .accessDeniedHandler(
                                        (req, res, auth) -> {
                                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                            res.getWriter().println("权限不足，请联系管理员!");
                                        }));

        http.addFilterBefore(loginFilter(), UsernamePasswordAuthenticationFilter.class);
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
                        csrf.disable());
                                // .csrfTokenRepository(
                                //         CookieCsrfTokenRepository
                                //                 .withHttpOnlyFalse())); // 将令牌保存到cookie中（并允许前端获取）
        // http.authenticationManager(
        //         new ProviderManager(Collections.singletonList(captchaAuthenticationProvider())));
        // http.authenticationProvider(); // 加入到当前 CustomAuthenticationManager 中，不推荐，应该在自定义 Filter
        // 中设置
        return http.build();
    }

    /**
     * 验证码认证过滤器.
     *
     * @return the captcha authentication filter
     */
    @Bean
    public AbstractAuthenticationProcessingFilter loginFilter() {
        final CaptchaAuthenticationFilter loginFilter =
                new CaptchaAuthenticationFilter(); // 配置 authenticationManager
        // final LoginFilter loginFilter = new LoginFilter();
        // loginFilter.setFilterProcessesUrl("/doLogin");
        // loginFilter.setUsernameParameter("uname");
        // loginFilter.setPasswordParameter("passwd");
        // loginFilter.setRememberMeServices(
        //         rememberMeServices()); // 前后端分离后，更改了获取方式后，存放也需要同步设置(2/2)
        // 指定认证管理器
        loginFilter.setAuthenticationManager(
                new ProviderManager(Collections.singletonList(captchaAuthenticationProvider())));
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
        return (web) ->
                web.ignoring().antMatchers("/withdraw/info", "/withdraw/hello", "/captcha/**");
    }

    /**
     * 自行实现根据手机号查询可用的用户，这里简单举例. 注意该接口可能出现多态。所以最好加上注解 @Qualifier
     *
     * @return the user details service
     */
    @Bean(name = "captchaUserDetailsService")
    public UserDetailsService captchaUserDetailsService() {
        // 验证码登陆后密码无意义了但是需要填充一下
        return username ->
                User.withUsername(username)
                        .password("TEMP")
                        // todo  这里权限 你需要自己注入
                        .authorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_APP"))
                        .build();
    }

    /**
     * 验证码认证器.
     *
     * @return the captcha authentication provider
     */
    @Bean
    CaptchaAuthenticationProvider captchaAuthenticationProvider() {
        return new CaptchaAuthenticationProvider(captchaUserDetailsService(), captchaService);
    }
}
