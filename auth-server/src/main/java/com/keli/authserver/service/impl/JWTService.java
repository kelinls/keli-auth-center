package com.keli.authserver.service.impl;

import com.keli.authserver.feign.TokenClient;
import com.keli.authserver.service.TokenService;
import com.keli.authserver.utils.PrincipalExtractUtil;
import com.keli.common.dto.JwtTokenGenerateRequest;
import com.keli.common.dto.SsoSessionPrincipal;
import com.keli.common.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.*;

import static com.baomidou.mybatisplus.extension.ddl.DdlScriptErrorHandler.PrintlnLogErrorHandler.log;

@Service
public class JWTService implements TokenService {
    @Autowired
    private AuthorizationServerSettings serverSettings;
    @Autowired
    private TokenClient tokenClient;

    @Override
    public AbstractOAuth2Token generateAccessToken(OAuth2TokenContext context) {
        JwtTokenGenerateRequest request = buildClaimsRequest(context, OAuth2TokenType.ACCESS_TOKEN);
        TokenResponse tokenResponse = tokenClient.generateToken(request);
        if(!tokenResponse.getTokenType().equals("access_token")) {
            throw new OAuth2AuthenticationException("token client return exception");
        }
        return new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                tokenResponse.getTokenValue(),
                Instant.ofEpochSecond(tokenResponse.getIssuedAt()),
                Instant.ofEpochSecond(tokenResponse.getExpiresAt()),
                context.getAuthorizedScopes() != null ? context.getAuthorizedScopes() : Set.of()
        );
    }

    @Override
    public AbstractOAuth2Token generateRefreshToken(OAuth2TokenContext context) {
        JwtTokenGenerateRequest request = buildClaimsRequest(context, OAuth2TokenType.REFRESH_TOKEN);
        TokenResponse tokenResponse = tokenClient.generateToken(request);
        if(!tokenResponse.getTokenType().equals("refresh_token")) {
            throw new OAuth2AuthenticationException("token client return exception");
        }
        return new OAuth2RefreshToken(tokenResponse.getTokenValue(),Instant.ofEpochSecond(tokenResponse.getIssuedAt()),Instant.ofEpochSecond(tokenResponse.getExpiresAt()));
    }

    @Override
    public AbstractOAuth2Token generateIdToken(OAuth2TokenContext context) {
        OAuth2TokenType idTokenType = new OAuth2TokenType(OidcParameterNames.ID_TOKEN);
        JwtTokenGenerateRequest request = buildClaimsRequest(context, idTokenType);
        TokenResponse tokenResponse = tokenClient.generateToken(request);
        if(!OidcParameterNames.ID_TOKEN.equals(tokenResponse.getTokenType())) {
            throw new OAuth2AuthenticationException("token client return exception");
        }
        Instant issuedAt = Instant.ofEpochSecond(tokenResponse.getIssuedAt());
        Instant expiresAt = Instant.ofEpochSecond(tokenResponse.getExpiresAt());
        //框架要求要使用Jwt类作为id_token的载体\
        return new Jwt(tokenResponse.getTokenValue(), issuedAt, expiresAt,tokenResponse.getHeaders(),request.getClaims());
    }

    @Override
    public AbstractOAuth2Token generateInternalAccessToken() {

        return null;
    }

    private JwtTokenGenerateRequest buildClaimsRequest(OAuth2TokenContext context, OAuth2TokenType tokenType){
        RegisteredClient client = context.getRegisteredClient();
        OAuth2Authorization authorization = context.getAuthorization();
        if (authorization == null) {
            throw new IllegalArgumentException("Authorization not found");
        }
        SsoSessionPrincipal principal = PrincipalExtractUtil.getPrincipal(authorization);

        Instant issuedAt = Instant.now();
        Map<String, Object> claims = new LinkedHashMap<>();
        //claims.put(JwtClaimNames.SUB,authorization.getPrincipalName());
        claims.put(JwtClaimNames.SUB, principal.getUid());
        claims.put("username", principal.getUsername());
        claims.put("client_id", client.getClientId());
        claims.put(JwtClaimNames.IAT, issuedAt.getEpochSecond());
        if (serverSettings.getIssuer() != null) {
            claims.put(JwtClaimNames.ISS, serverSettings.getIssuer());
        }
        //存放token对应的权限，理论上是只有accessToken需要的，其他的可以不放
        if (tokenType.equals(OAuth2TokenType.ACCESS_TOKEN) && principal != null && principal.getRoles() != null) {
            claims.put("roles", principal.getRoles());
        }

        long ttlSeconds;
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            ttlSeconds = client.getTokenSettings().getAccessTokenTimeToLive().getSeconds();
            claims.put("scope", String.join(" ", context.getAuthorizedScopes() != null ? context.getAuthorizedScopes() : Set.of()));
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            ttlSeconds = client.getTokenSettings().getRefreshTokenTimeToLive().getSeconds();
        } else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            ttlSeconds = client.getTokenSettings().getAccessTokenTimeToLive().getSeconds();
            claims.put(JwtClaimNames.AUD, new ArrayList<>(List.of(client.getClientId())));
            claims.put("azp", client.getClientId());
            if (principal != null && principal.getSessionId() != null && !principal.getSessionId().isBlank()) {
                // 供 OIDC RP-Initiated Logout 在 OP 侧定位会话
                //一定要先加密下sessionId不然会报错
                try {
                    claims.put("sid", createHash(principal.getSessionId()));
                } catch (NoSuchAlgorithmException e) {
                    log.error("添加sid失败");
                    throw new RuntimeException(e);
                }
            }
            Object nonce = resolveNonce(authorization);
            if (nonce != null) {
                claims.put("nonce", nonce);
            }
        } else {
            throw new IllegalArgumentException("Unsupported token type: " + tokenType.getValue());
        }

        claims.put(JwtClaimNames.EXP, issuedAt.plusSeconds(ttlSeconds).getEpochSecond());

        Map<String, Object> additionalClaims = new HashMap<>();
        Map<String, Object> attributes = authorization.getAttributes();
        if (attributes.containsKey("tenant_id")) {
            additionalClaims.put("tenant_id", attributes.get("tenant_id"));
        }

        claims.putAll(additionalClaims);

        return JwtTokenGenerateRequest.builder()
                .tokenType(tokenType.getValue())
                .authorizationId(authorization.getId())
                .claims(claims)
                .build();
    }

    /**
     * OIDC nonce 在不同链路里可能位于不同 attribute key，做兼容提取。
     */
    private Object resolveNonce(OAuth2Authorization authorization) {
        Object direct = authorization.getAttribute("nonce");
        if (direct != null) {
            return direct;
        }
        OAuth2AuthorizationRequest authorizationRequest =
                authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
        if (authorizationRequest == null || authorizationRequest.getAdditionalParameters() == null) {
            return null;
        }
        return authorizationRequest.getAdditionalParameters().get(OidcParameterNames.NONCE);
    }

    /**
     * 与 Spring Authorization Server 的 OIDC Logout 校验逻辑保持一致：
     * id_token 的 sid 应为 sessionId 的 SHA-256 Base64URL（不带 padding）哈希值。
     */
    private  String createHash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
