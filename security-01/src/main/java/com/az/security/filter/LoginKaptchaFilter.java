package com.az.security.filter;

import com.az.security.exception.KaptchaNoMatchException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * LoginKaptcha
 *
 * @author zy
 * @version 2022/5/16
 */
@Slf4j
public class LoginKaptchaFilter extends UsernamePasswordAuthenticationFilter {

    public static final String SPRING_SECURITY_FORM_KAPTCHA_KEY = "kaptcha";

    private String kaptchaParameter = SPRING_SECURITY_FORM_KAPTCHA_KEY;

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        try {
            final Map<String, String> userInfo =
                    new ObjectMapper().readValue(request.getInputStream(), Map.class);
            final String kaptcha = userInfo.get(getKaptchaParameter());
            final String username = userInfo.get(getUsernameParameter());
            final String password = userInfo.get(getPasswordParameter());

            final String sessionVerifyCode = (String) request.getSession().getAttribute("kaptcha");
            log.info(sessionVerifyCode, kaptcha);
            if (!ObjectUtils.isEmpty(sessionVerifyCode)
                    && !ObjectUtils.isEmpty(kaptcha)
                    && kaptcha.equalsIgnoreCase(sessionVerifyCode)) {
                final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(username, password);
                setDetails(request, usernamePasswordAuthenticationToken);
                return this.getAuthenticationManager()
                        .authenticate(usernamePasswordAuthenticationToken);
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
        throw new KaptchaNoMatchException("验证码错误");
    }

    @Override
    public void setPasswordParameter(String passwordParameter) {
        super.setPasswordParameter(passwordParameter);
    }

    public String getKaptchaParameter() {
        return kaptchaParameter;
    }

    public void setKaptchaParameter(String kaptchaParameter) {
        this.kaptchaParameter = kaptchaParameter;
    }
}
