package com.armin.security.security.filter;

import com.armin.security.security.CaptchaAuthenticationToken;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 模仿 UsernamePasswordAuthenticationFilter 100% 还原度
 *
 * @author armin
 */
public class CaptchaAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_FORM_PHONE_KEY = "phone";
    public static final String SPRING_SECURITY_FORM_CAPTCHA_KEY = "captcha";
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher("/captchaLogin", "POST");

    public CaptchaAuthenticationFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        String phone = obtainPhone(request);
        phone = (phone != null) ? phone : "";
        phone = phone.trim();
        String captcha = obtainCaptcha(request);
        captcha = (captcha != null) ? captcha : "";
        CaptchaAuthenticationToken authRequest = new CaptchaAuthenticationToken(phone, captcha);
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Nullable
    protected String obtainCaptcha(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_CAPTCHA_KEY);
    }

    @Nullable
    protected String obtainPhone(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_PHONE_KEY);
    }

    protected void setDetails(HttpServletRequest request, CaptchaAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }
}
