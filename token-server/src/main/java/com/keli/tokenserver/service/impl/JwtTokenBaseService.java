package com.keli.tokenserver.service.impl;


import com.keli.common.dto.TokenRequest;
import com.keli.tokenserver.service.TokenBaseService;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenBaseService implements TokenBaseService {

    @Override
    public JwtClaimsSet generateAccessToken(TokenRequest request) {
        // 构建 JWT 声明
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(request.getSubject())
                .issuedAt(Instant.now())
                .claim("client_id", request.getClientId())
                .claim("roles", request.getRoles());
        if(request.getIssuer() != null) {
            claimsBuilder.issuer(request.getIssuer());
        }
        // 设置有效期 access
        long ttl = request.getAccessTokenTtl() != null ? request.getAccessTokenTtl() : 360;
            claimsBuilder.expiresAt(Instant.now().plusSeconds(ttl));
            // 添加 scope
            if (request.getScope() != null) {
                claimsBuilder.claim("scope", request.getScope());
            }


        // 添加自定义声明
        if (request.getAdditionalClaims() != null) {
            request.getAdditionalClaims().forEach(claimsBuilder::claim);
        }

        JwtClaimsSet claims = claimsBuilder.build();
      return claims;
    }

    @Override
    public JwtClaimsSet generateRefreshToken(TokenRequest request) {
        // 构建 JWT 声明
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(request.getSubject())
                .issuedAt(Instant.now())
                .claim("client_id", request.getClientId())
                .claim("roles", request.getRoles());

        if(request.getIssuer() != null) {
            claimsBuilder.issuer(request.getIssuer());
        }
        // 设置有效期 refresh
        long ttl = request.getRefreshTokenTtl() != null ? request.getRefreshTokenTtl() : 72000;
            claimsBuilder.expiresAt(Instant.now().plusSeconds(ttl));
            // 刷新令牌可包含额外信息，但不需要 scope

        // 添加自定义声明
        if (request.getAdditionalClaims() != null) {
            request.getAdditionalClaims().forEach(claimsBuilder::claim);
        }

        JwtClaimsSet claims = claimsBuilder.build();
      return claims;
    }
}
