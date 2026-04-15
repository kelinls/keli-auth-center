package com.keli.authserver.feign;

import com.keli.common.dto.JwtTokenGenerateRequest;
import com.keli.common.dto.TokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "token-server")
public interface TokenClient {
    @PostMapping("/api/token/generate")
    TokenResponse generateToken(@RequestBody JwtTokenGenerateRequest request);
    @GetMapping("/oauth2/jwks")
    Map<String,Object> jwksEndpoint();
}
