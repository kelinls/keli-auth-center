package com.keli.sessionserver.service;

import com.keli.common.dto.SsoTokenCredentials;
import com.keli.sessionserver.dto.SessionData;

public interface SessionSecurityValidator {
    boolean validate(SessionData sessionData, SsoTokenCredentials credentials);
}
