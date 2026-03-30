package com.keli.authserver.service.impl;

import com.keli.authserver.dto.SsoSessionAuthenticationToken;
import com.keli.authserver.dto.UserInfo;
import com.keli.authserver.feign.SessionClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
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
        String sessionId = token.getSessionId();

        // 调用 SSO 服务验证 sessionId，返回用户信息
        UserInfo userInfo =  sessionClient.validateSession(sessionId);
        if (userInfo == null) {
            throw new BadCredentialsException("Invalid SSO session");
        }
        // 返回认证成功的 Token，成功认证是因为这个构造器中setAuthenticated(true);
        return new SsoSessionAuthenticationToken(sessionId, userInfo, null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SsoSessionAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
