package com.eastcom.ecfc.security;

import com.eastcom.ecfc.exception.KaptchaNoMatchException;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * KaptchaFilter
 *
 * @author zy
 * @version 2022/5/14
 */
public class KaptchaFilter extends UsernamePasswordAuthenticationFilter {

    public static final String SPRING_SECURITY_FORM_KAPTCHA_KEY = "kaptcha";

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        /*
        1 判断是否 post 方式请求
        2 判断是否 json 格式请求类型
        3 从 json 数据中获取用户输入用户名密码进行验证
         */
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationServiceException("method not support: " + request.getMethod());
        }
        String kaptcha = request.getParameter(SPRING_SECURITY_FORM_KAPTCHA_KEY);
        String attribute =
                (String) request.getSession().getAttribute(SPRING_SECURITY_FORM_KAPTCHA_KEY);
        if (!ObjectUtils.isEmpty(attribute)
                && !ObjectUtils.isEmpty(kaptcha)
                && kaptcha.equalsIgnoreCase(attribute)) {
            return super.attemptAuthentication(request, response);
        }
        throw new KaptchaNoMatchException("验证码不匹配");
    }

    public static void main(String[] args) {
        System.out.println();
    }
}
