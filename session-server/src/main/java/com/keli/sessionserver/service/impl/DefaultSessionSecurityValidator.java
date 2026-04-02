package com.keli.sessionserver.service.impl;

import com.keli.common.dto.SsoTokenCredentials;
import com.keli.sessionserver.dto.SessionData;
import com.keli.sessionserver.service.SessionSecurityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Primary
public class DefaultSessionSecurityValidator implements SessionSecurityValidator {
    @Override
    public boolean validate(SessionData sessionData, SsoTokenCredentials credentials) {
        if(!credentials.getClientIp().equals(sessionData.getClientIp())) {
            log.warn("new ip login:{}",credentials.getClientIp());
            logIpChange(credentials.getClientIp(),credentials.getSessionId(),sessionData.getUid());
        }
        //新的userAgent，sessionId可以遭劫持，拒绝
        if(!credentials.getUserAgent().equals(sessionData.getUserAgent())) {
            log.warn("new device request:{}",credentials.getUserAgent());
            //测试先设为true
            return false;
        }
        return true;
    }

    private void logIpChange(String clientIp, String sessionId, String uid) {
    }
}
