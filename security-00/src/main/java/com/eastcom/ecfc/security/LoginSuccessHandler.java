package com.eastcom.ecfc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginSuccessHandler
 *
 * @author zy
 * @version 2022/5/6
 */
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    public static Object getCurrentUser(Authentication authentication) {
        if (authentication != null) {
            return authentication.getPrincipal();
        }
        return null;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");

        Object currentUser = getCurrentUser(authentication);
        request.getSession().setAttribute("userInfo", currentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("userInfo", currentUser);

        PrintWriter out = response.getWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        out.write(objectMapper.writeValueAsString(data));
        out.flush();
        out.close();
    }
}
