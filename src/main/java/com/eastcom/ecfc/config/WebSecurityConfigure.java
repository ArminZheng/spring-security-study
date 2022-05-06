package com.eastcom.ecfc.config;

import com.eastcom.ecfc.security.LoginFailureHandler;
import com.eastcom.ecfc.security.LoginSuccessHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

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
                .mvcMatchers("/logout").permitAll()
                .mvcMatchers("/index").permitAll()
                // 放行资源要写在【任何】前面
                .anyRequest().authenticated()
                .and() // 匿名内部类中使用: 类名.this.属性名,调用外部类属性 e.g.
                // ExpressionUrlAuthorizationConfigurer.this.and();
                .formLogin()
                .loginPage("/login.html") // 指定登陆页面，一旦定义必须指定登陆api
                .loginProcessingUrl("/doLogin") // 指定登陆api ，必须同时指定登陆页面
                .usernameParameter("uname")
                .passwordParameter("pwd")
                // .successForwardUrl("/hello") // forward 转发，url不变 （只能二选一）
                // .defaultSuccessUrl("/hello") // redirect 重定向，url改变（只能二选一）
                // .defaultSuccessUrl("/index", true) // default的特性，如果之前访问受限资源，会优先上一次。需要设为true才能强转
                .successHandler(new LoginSuccessHandler())
                // .failureForwardUrl("/login.html")
                // .failureUrl("/login.html")
                .failureHandler(new LoginFailureHandler())
                .and()
                .logout()
                // .logoutUrl("/logout") // default true
                .logoutRequestMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/aa", "GET"),
                        new AntPathRequestMatcher("/bb", "POST")
                ))
                .invalidateHttpSession(true) // default true
                .clearAuthentication(true) // default true
                .logoutSuccessUrl("/login.html")

                // 以后再说
                .and().csrf().disable(); // 禁止 csrf 跨站请求保护
    }
}
