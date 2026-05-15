package com.keli.authserver.service.impl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Locale;

public class ExternalLoginAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final String loginUrl;
    public ExternalLoginAuthenticationEntryPoint(String loginUrl) {
        this.loginUrl = loginUrl;
    }
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 获取原始请求的全路径（含查询参数），用于登录后跳回
        String queryString = request.getQueryString();
        String requestUri = request.getRequestURI();
        String gatewayRequestUri = requestUri.startsWith("/auth") ? requestUri : "/auth" + requestUri;
        String fullUrl = buildExternalBaseUrl(request) + gatewayRequestUri + (queryString != null ? "?" + queryString : "");
        
        // 构造重定向到登录页的URL，并携带 authorizationRequest 参数
        String redirectUrl = loginUrl + "?authorizationRequest=" + java.net.URLEncoder.encode(fullUrl, "UTF-8");
        
        response.sendRedirect(redirectUrl);
    }

    private String buildExternalBaseUrl(HttpServletRequest request) {
        String forwardedProto = firstHeaderValue(request, "X-Forwarded-Proto");
        String forwardedHost = firstHeaderValue(request, "X-Forwarded-Host");
        String forwardedPort = firstHeaderValue(request, "X-Forwarded-Port");

        String scheme = (forwardedProto != null && !forwardedProto.isBlank()) ? forwardedProto : request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        if (forwardedHost != null && !forwardedHost.isBlank()) {
            String normalizedHost = forwardedHost;
            if (normalizedHost.contains(",")) {
                normalizedHost = normalizedHost.split(",")[0].trim();
            }
            if (normalizedHost.contains(":")) {
                int lastColonIndex = normalizedHost.lastIndexOf(':');
                host = normalizedHost.substring(0, lastColonIndex);
                try {
                    port = Integer.parseInt(normalizedHost.substring(lastColonIndex + 1));
                } catch (NumberFormatException ignored) {
                    // keep existing port
                }
            } else {
                host = normalizedHost;
            }
        }

        if (forwardedPort != null && !forwardedPort.isBlank()) {
            try {
                port = Integer.parseInt(forwardedPort);
            } catch (NumberFormatException ignored) {
                // keep existing port
            }
        }

        String base = scheme + "://" + host;
        if (!isDefaultPort(scheme, port)) {
            base += ":" + port;
        }
        return base;
    }

    private String firstHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.split(",")[0].trim();
    }

    private boolean isDefaultPort(String scheme, int port) {
        if (port <= 0) {
            return true;
        }
        String normalizedScheme = scheme == null ? "" : scheme.toLowerCase(Locale.ROOT);
        return ("http".equals(normalizedScheme) && port == 80) || ("https".equals(normalizedScheme) && port == 443);
    }
}
