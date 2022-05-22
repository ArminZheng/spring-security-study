package com.eastcom.ecfc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class LabelFilter extends GenericFilterBean {

    public LabelFilter() {}

    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        System.err.printf(
                "请求URL:[%s?%s], 请求IP:[%s]\n",
                req.getRequestURL(),
                !req.getParameterNames().hasMoreElements()
                        ? null
                        : req.getParameterNames().nextElement(),
                req.getRemoteAddr());
        chain.doFilter(req, res);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doFilterInternal((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }
}
