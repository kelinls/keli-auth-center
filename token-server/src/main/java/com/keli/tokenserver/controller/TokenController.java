package com.keli.tokenserver.controller;

import com.keli.common.dto.JwtTokenGenerateRequest;
import com.keli.common.dto.TokenResponse;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/token")
public class TokenController {
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private JWKSet jwkSet;

    @PostMapping("/generate")
    public TokenResponse generate(@RequestBody JwtTokenGenerateRequest request) {
        JwtClaimsSet claims = buildClaims(request.getClaims());
        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        //获取创建token所使用的key
        JWK key = jwkSet.getKeyByKeyId("kelinls-kid");
        Map<String,Object> headers = new HashMap<>();
        headers.put("alg",key.getAlgorithm());
        headers.put("typ","JWT");
        headers.put("kid",key.getKeyID());

        Instant issuedAt = claims.getIssuedAt() != null ? claims.getIssuedAt() : Instant.now();
        Instant expiresAt = claims.getExpiresAt() != null ? claims.getExpiresAt() : issuedAt;
        return TokenResponse.builder()
                .tokenValue(jwt)
                .headers(headers)
                .tokenType(request.getTokenType())
                .expiresIn(Math.max(0L, expiresAt.getEpochSecond() - issuedAt.getEpochSecond()))
                .issuedAt(issuedAt.getEpochSecond())
                .expiresAt(expiresAt.getEpochSecond())
                .authorizationId(request.getAuthorizationId())
                .build();
    }

    private JwtClaimsSet buildClaims(Map<String, Object> claimMap) {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder();
        if (claimMap == null || claimMap.isEmpty()) {
            return builder.issuedAt(Instant.now()).build();
        }

        claimMap.forEach((k, v) -> {
            switch (k) {
                case JwtClaimNames.SUB -> builder.subject(asString(v));
                case JwtClaimNames.ISS -> builder.issuer(asString(v));
                case JwtClaimNames.AUD -> {
                    if (v instanceof List<?> list) {
                        builder.audience(list.stream().map(String::valueOf).toList());
                    }
                }
                case JwtClaimNames.IAT -> builder.issuedAt(asInstant(v));
                case JwtClaimNames.EXP -> builder.expiresAt(asInstant(v));
                case JwtClaimNames.NBF -> builder.notBefore(asInstant(v));
                default -> builder.claim(k, v);
            }
        });
        if (builder.build().getIssuedAt() == null) {
            builder.issuedAt(Instant.now());
        }
        return builder.build();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Instant asInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Number number) {
            return Instant.ofEpochSecond(number.longValue());
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.matches("^\\d+$")) {
            return Instant.ofEpochSecond(Long.parseLong(text));
        }
        return Instant.parse(text);
    }
}
