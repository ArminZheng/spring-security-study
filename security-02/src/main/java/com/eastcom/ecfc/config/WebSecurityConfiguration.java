package com.eastcom.ecfc.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import javax.servlet.http.HttpServletResponse;

/**
 * WebSecurityConfiguration
 *
 * @author zy
 * @version 2022/5/23
 */
@Configuration
@AllArgsConstructor
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/withdraw/info")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .and()
                .csrf();
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
                .sessionRegistry(sessionRegistry());
    }

    private final FindByIndexNameSessionRepository sessionRepository;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry(sessionRepository);
    }
}
