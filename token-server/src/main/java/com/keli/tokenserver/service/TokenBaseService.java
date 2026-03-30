package com.keli.tokenserver.service;

import com.keli.tokenserver.dto.TokenRequest;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

public interface TokenBaseService {
    JwtClaimsSet generateAccessToken(TokenRequest request);
    JwtClaimsSet generateRefreshToken(TokenRequest request);
}
