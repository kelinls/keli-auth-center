package com.keli.authserver.filter;

import com.keli.authserver.dto.SsoSessionAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Slf4j
public class SsoSessionAuthenticationFilter extends OncePerRequestFilter {

     private AuthenticationManager authenticationManager;
     public SsoSessionAuthenticationFilter( AuthenticationManager authenticationManager) {
         this.authenticationManager = authenticationManager;
     }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
         ;

        // 如果已经认证，跳过
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从 Cookie 或 Header 中获取 sessionId
        String sessionId = extractSessionId(request);
        if (sessionId != null) {
            //授权成功与否，取决于authResult.isAuthenticated()，也就是token的父类中的private boolean authenticated = false
            // 默认是false，只要认证返回的authResult中是true就成功了
            SsoSessionAuthenticationToken token = new SsoSessionAuthenticationToken(sessionId,request);
            try {
                Authentication authResult = authenticationManager.authenticate(token);
                if (authResult.isAuthenticated()) {
                    SecurityContextHolder.getContext().setAuthentication(authResult);
                    request.getSession().setAttribute(
                            "SPRING_SECURITY_CONTEXT",
                            SecurityContextHolder.getContext()
                    );
                }
            } catch (AuthenticationException e) {
                // 验证失败，继续处理（可能会触发 AuthenticationEntryPoint）
                log.debug("SSO session validation failed {}",e.toString());
            }
        }

    }

    private String extractSessionId(HttpServletRequest request) {
        return "testSessionId";
    }
}
