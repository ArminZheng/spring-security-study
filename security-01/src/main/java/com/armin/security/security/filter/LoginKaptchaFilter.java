package com.armin.security.security.filter;

import com.armin.security.security.exception.KaptchaNoMatchException;
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
        if (!request.getMethod().equals("POST")) { // 模仿 UsernamePasswordAuthenticationFilter
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        try { // 开始获取参数, 进行校验, 通过后放行到下一个 Filter中
            final Map<String, String> userInfo =
                    new ObjectMapper().readValue(request.getInputStream(), Map.class);
            final String kaptcha = userInfo.get(getKaptchaParameter());
            final String username = userInfo.get(getUsernameParameter());
            final String password = userInfo.get(getPasswordParameter());

            final String sessionVerifyCode = (String) request.getSession().getAttribute("kaptcha");
            log.info(sessionVerifyCode + " versus " + kaptcha);
            if (!ObjectUtils.isEmpty(sessionVerifyCode) // 验证码是否输入, 是否存在, 是否相等 (否则下一步抛异常)
                    && !ObjectUtils.isEmpty(kaptcha)
                    && kaptcha.equalsIgnoreCase(sessionVerifyCode)) {
                final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(username, password);
                // 将request请求信息也放置在Token里面 (IP，session 或 角色信息)
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
