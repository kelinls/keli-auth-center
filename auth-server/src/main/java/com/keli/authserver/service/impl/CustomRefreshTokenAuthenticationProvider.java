package com.keli.authserver.service.impl;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CustomRefreshTokenAuthenticationProvider implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final RegisteredClientRepository registeredClientRepository;

    public CustomRefreshTokenAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                                    OAuth2TokenGenerator<?> tokenGenerator,
                                                    RegisteredClientRepository registeredClientRepository) {
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.registeredClientRepository = registeredClientRepository;
    }
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2RefreshTokenAuthenticationToken refreshTokenAuth =
                (OAuth2RefreshTokenAuthenticationToken) authentication;

        // 1. 提取刷新令牌
        String refreshTokenValue = refreshTokenAuth.getRefreshToken();

        // 2. 查找授权记录（从存储中获取，支持缓存）
        OAuth2Authorization authorization = authorizationService.findByToken(refreshTokenValue, OAuth2TokenType.REFRESH_TOKEN);
        if (authorization == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT), "Invalid refresh token");
        }

        // 3. 获取关联的客户端
        RegisteredClient registeredClient = registeredClientRepository.findById(authorization.getRegisteredClientId());
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT), "Invalid client");
        }

//        // 4. 验证客户端是否匹配（请求中的 client_id 必须与授权记录中的一致）
//        if (!registeredClient.getClientId().equals(refreshTokenAuth)) {
//            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT), "Client mismatch");
//        }

        // 5. 验证刷新令牌是否有效（存在且未过期）
        OAuth2RefreshToken storedRefreshToken = null;
        if(authorization.getRefreshToken() != null){
            storedRefreshToken = authorization.getRefreshToken().getToken();
        }

        if (storedRefreshToken == null || !storedRefreshToken.getTokenValue().equals(refreshTokenValue)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT), "Invalid refresh token");
        }
        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT), "Refresh token expired");
        }

        // 6. 可选的额外验证：检查刷新令牌是否被撤销（如果维护了黑名单）
        // ...

        // 7. 生成新的访问令牌
        OAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal((Authentication) refreshTokenAuth.getPrincipal())
                .authorization(authorization)
                .authorizedScopes(authorization.getAuthorizedScopes())
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();
        OAuth2AccessToken accessToken = (OAuth2AccessToken) tokenGenerator.generate(accessTokenContext);
        if (accessToken == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR), "Access token generation failed");
        }

        // 8. 决定是否轮换刷新令牌
        OAuth2RefreshToken newRefreshToken = null;
        if (registeredClient.getTokenSettings().isReuseRefreshTokens()) {
            // 不轮换：保留原有的刷新令牌
            newRefreshToken = storedRefreshToken;
        } else {
            // 轮换：生成新的刷新令牌
            OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal((Authentication) refreshTokenAuth.getPrincipal())
                    .authorization(authorization)
                    .authorizedScopes(authorization.getAuthorizedScopes())
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .build();
            newRefreshToken = (OAuth2RefreshToken) tokenGenerator.generate(refreshTokenContext);
            if (newRefreshToken == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR), "Refresh token generation failed");
            }
        }

        // 9. 更新授权记录（存储新的访问令牌和刷新令牌）
        OAuth2Authorization updatedAuthorization = OAuth2Authorization.from(authorization)
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
        authorizationService.save(updatedAuthorization);

        // 10. 返回认证成功结果
        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                (Authentication) refreshTokenAuth.getPrincipal(),
                accessToken,
                newRefreshToken,
                refreshTokenAuth.getAdditionalParameters()  //额外参数
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2RefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
