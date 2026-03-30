package com.keli.authserver.service;

import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;

public interface TokenService {
    AbstractOAuth2Token generateAccessToken(OAuth2TokenContext context);
    AbstractOAuth2Token generateRefreshToken(OAuth2TokenContext context);
}
