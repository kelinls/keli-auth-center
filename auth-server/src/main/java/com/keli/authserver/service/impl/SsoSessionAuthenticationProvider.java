package com.keli.authserver.service.impl;

import com.keli.authserver.dto.SsoSessionAuthenticationToken;
import com.keli.authserver.feign.SessionClient;
import com.keli.common.dto.SsoSessionPrincipal;
import com.keli.common.dto.SsoTokenCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SsoSessionAuthenticationProvider implements AuthenticationProvider {

    private final SessionClient sessionClient;

    public SsoSessionAuthenticationProvider(SessionClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        SsoSessionAuthenticationToken token = (SsoSessionAuthenticationToken) authentication;
        SsoTokenCredentials credentials = (SsoTokenCredentials) token.getCredentials();

        // 调用 SSO 服务验证 sessionId，返回用户信息
        SsoSessionPrincipal principal =  sessionClient.validateSession(credentials);
        if (principal == null) {
            return token;
        }
        // 返回认证成功的 Token，成功认证是因为这个构造器中setAuthenticated(true);
        return new SsoSessionAuthenticationToken(principal, null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SsoSessionAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
