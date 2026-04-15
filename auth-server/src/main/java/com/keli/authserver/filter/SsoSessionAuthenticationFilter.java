package com.keli.authserver.filter;

import com.keli.authserver.dto.SsoSessionAuthenticationToken;
import com.keli.authserver.feign.SessionClient;
import com.keli.common.dto.SsoSessionPrincipal;
import com.keli.common.dto.SsoTokenCredentials;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class SsoSessionAuthenticationFilter extends OncePerRequestFilter {
    private static final String OIDC_LOGOUT_ENDPOINT = "/connect/logout";

    private final AuthenticationManager authenticationManager;
    //如果配置了 SecurityContextRepository 为 HttpSessionSecurityContextRepository（默认），那么当请求结束时，如果 SecurityContext 有变化，Spring Security 会将其自动存入 Session（可能创建新的 Session）
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SessionClient sessionClient;
    private final SessionRegistry sessionRegistry;

    public SsoSessionAuthenticationFilter(AuthenticationManager authenticationManager,
                                          SessionClient sessionClient,
                                          SessionRegistry sessionRegistry) {
        this.authenticationManager = authenticationManager;
        this.sessionClient = sessionClient;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (OIDC_LOGOUT_ENDPOINT.equals(request.getRequestURI())) {
            // 对 RP-Initiated Logout 不注入当前登录态，避免框架走 OP HttpSession sid 校验分支
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // 从 Cookie 中获取 sessionId
        String sessionId = extractSessionId(request);
        if (sessionId != null) {
            log.debug("Found SESSION_ID cookie: {}, attempting authentication", sessionId);
            SsoTokenCredentials ssoTokenCredentials = new SsoTokenCredentials();
            ssoTokenCredentials.setSessionId(sessionId);
            ssoTokenCredentials.setClientIp(request.getRemoteAddr());
            ssoTokenCredentials.setUserAgent(request.getHeader("User-Agent"));
            // 如果已经认证
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                //即使认证过了也要验证一遍session保证一致性
                SsoSessionPrincipal principal = sessionClient.validateSession(ssoTokenCredentials);
                //已经有了认证但是验证失败
                if (principal == null) {
                    SecurityContextHolder.clearContext();
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                }
                //未被清除就直接放行
                filterChain.doFilter(request, response);
                return;
            }
            //未认证创建authorization
            SsoSessionAuthenticationToken token = new SsoSessionAuthenticationToken(ssoTokenCredentials);
            try {
                Authentication authResult = authenticationManager.authenticate(token);
                if (authResult.isAuthenticated()) {
                    log.debug("SSO authentication success for user: {}", authResult.getPrincipal());
                    if (authResult.getPrincipal() instanceof SsoSessionPrincipal principal
                            && principal.getSessionId() != null
                            && !principal.getSessionId().isBlank()) {
                        // 让 OIDC logout 能通过 sid 在 SessionRegistry 中定位会话
                        sessionRegistry.removeSessionInformation(principal.getSessionId());
                        sessionRegistry.registerNewSession(principal.getSessionId(), principal);
                        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(principal.getSessionId());
                        log.info(sessionInformation.getSessionId());
                    }
                    // 创建新的 SecurityContext 并保存
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authResult);
                    SecurityContextHolder.setContext(context);
                    
                    // 必须显式保存到 Repository，否则在 Spring Security 6 中后续过滤器可能无法识别
                    securityContextRepository.saveContext(context, request, response);
                } else {
                    log.debug("SSO authentication returned unauthenticated token");
                }
            } catch (AuthenticationException e) {
                log.debug("SSO authentication failed: {}", e.getMessage());
            }
        } else {
            //sessionId都不存在
            //无论有无认证过都清除一遍
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            log.debug("No SESSION_ID cookie found in request to {}", request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }

    private String extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("SESSION_ID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
