package com.keli.authserver.service.impl;

import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
public class CustomTokenGenerator implements OAuth2TokenGenerator<AbstractOAuth2Token> {
    private final JWTService jwtService;

    public CustomTokenGenerator(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public AbstractOAuth2Token generate(OAuth2TokenContext context) {
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            return jwtService.generateAccessToken(context);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
            return jwtService.generateRefreshToken(context);
        } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            return jwtService.generateIdToken(context);
        }
        return null;
    }
}
