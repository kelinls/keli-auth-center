package com.keli.authserver.service.impl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class ExternalLoginAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final String loginUrl;
    public ExternalLoginAuthenticationEntryPoint(String loginUrl) {
        this.loginUrl = loginUrl;
    }
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 获取原始请求的全路径（含查询参数），用于登录后跳回
        String queryString = request.getQueryString();
        String fullUrl = request.getRequestURL().toString() + (queryString != null ? "?" + queryString : "");
        System.out.println(fullUrl);
        
        // 构造重定向到登录页的URL，并携带 authorizationRequest 参数
        String redirectUrl = loginUrl + "?authorizationRequest=" + java.net.URLEncoder.encode(fullUrl, "UTF-8");
        
        response.sendRedirect(redirectUrl);
    }
}
