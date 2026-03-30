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
        response.sendRedirect(loginUrl);
    }
}
