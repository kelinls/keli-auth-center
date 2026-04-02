package com.keli.authserver.service.impl;

import com.keli.authserver.feign.TokenClient;
import com.keli.authserver.service.TokenService;
import com.keli.common.dto.TokenRequest;
import com.keli.common.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService implements TokenService {
    @Autowired
    private AuthorizationServerSettings serverSettings;
    @Autowired
    private TokenClient tokenClient;

    @Override
    public AbstractOAuth2Token generateAccessToken(OAuth2TokenContext context) {
        TokenRequest tokenRequest = buildRequest(context);
        TokenResponse tokenResponse = tokenClient.accessTokenGenerate(tokenRequest);
        if(!tokenResponse.getTokenType().equals("access_token")) {
            throw new OAuth2AuthenticationException("token client return exception");
        }
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,tokenResponse.getTokenValue(), Instant.ofEpochSecond(tokenResponse.getIssuedAt()),Instant.ofEpochSecond(tokenResponse.getExpiresAt()));
    }

    @Override
    public AbstractOAuth2Token generateRefreshToken(OAuth2TokenContext context) {
        TokenRequest tokenRequest = buildRequest(context);
        TokenResponse tokenResponse = tokenClient.accessTokenGenerate(tokenRequest);
        if(!tokenResponse.getTokenType().equals("refresh_token")) {
            throw new OAuth2AuthenticationException("token client return exception");
        }
        return new OAuth2RefreshToken(tokenResponse.getTokenValue(),Instant.ofEpochSecond(tokenResponse.getIssuedAt()),Instant.ofEpochSecond(tokenResponse.getExpiresAt()));
    }
    private TokenRequest buildRequest(OAuth2TokenContext context){
        RegisteredClient client = context.getRegisteredClient();
        OAuth2Authorization authorization = context.getAuthorization();
        OAuth2TokenType tokenType = context.getTokenType();

        // 基础信息
        TokenRequest.TokenRequestBuilder builder = TokenRequest.builder()
                .clientId(client.getClientId())
                .subject(authorization.getPrincipalName())
                .scope(String.join(" ", context.getAuthorizedScopes()))
                .tokenType(tokenType.getValue())
                .issuer(serverSettings.getIssuer())
                .authorizationId(authorization.getId());

        // 有效期
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            builder.accessTokenTtl(client.getTokenSettings().getAccessTokenTimeToLive().getSeconds());
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            builder.refreshTokenTtl(client.getTokenSettings().getRefreshTokenTimeToLive().getSeconds());
            // 如果是刷新令牌场景，可能还需要传递旧的刷新令牌（用于验证或轮换）
            OAuth2RefreshToken existingToken = null;
            if(authorization.getRefreshToken() != null) {
                existingToken = authorization.getRefreshToken().getToken();
            }

            if (existingToken != null) {
                builder.previousRefreshToken(existingToken.getTokenValue());
            }
        }

        // 附加声明：从授权记录的 attributes 中提取自定义参数
        Map<String, Object> additionalClaims = new HashMap<>();
        Map<String, Object> attributes = authorization.getAttributes();
        // 假设授权请求时存入了 tenant_id
        if (attributes.containsKey("tenant_id")) {
            additionalClaims.put("tenant_id", attributes.get("tenant_id"));
        }
        // 还可以添加其他自定义字段
        builder.additionalClaims(additionalClaims);
        return builder.build();
    }
}
