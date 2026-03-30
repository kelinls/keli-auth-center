package com.keli.tokenserver.controller;

import com.keli.tokenserver.dto.TokenRequest;
import com.keli.tokenserver.dto.TokenResponse;
import com.keli.tokenserver.service.TokenBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("api/token")
public class TokenController {
    @Autowired
    private TokenBaseService tokenBaseService;
    @Autowired
    private JwtEncoder jwtEncoder;
    @PostMapping("/generateAccess")
    public TokenResponse accessTokenGenerate(@RequestBody TokenRequest tokenRequest) {

        JwtClaimsSet claims = tokenBaseService.generateAccessToken(tokenRequest);
        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return TokenResponse.builder()
                .tokenValue(jwt)
                .tokenType(tokenRequest.getTokenType())
                .expiresIn(tokenRequest.getAccessTokenTtl())
                .issuedAt(Instant.now().getEpochSecond())
                .expiresAt(claims.getExpiresAt().getEpochSecond())
                .authorizationId(tokenRequest.getAuthorizationId())
                .build();
    }
    @PostMapping("/generateRefresh")
    public TokenResponse refreshTokenGenerate(@RequestBody TokenRequest tokenRequest) {
        JwtClaimsSet claims = tokenBaseService.generateRefreshToken(tokenRequest);
        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return TokenResponse.builder()
                .tokenValue(jwt)
                .tokenType(tokenRequest.getTokenType())
                .expiresIn(tokenRequest.getRefreshTokenTtl())
                .issuedAt(Instant.now().getEpochSecond())
                .expiresAt(claims.getExpiresAt().getEpochSecond())
                .authorizationId(tokenRequest.getAuthorizationId())
                .build();
    }


}
