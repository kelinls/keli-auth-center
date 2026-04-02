package com.keli.authserver.feign;

import com.keli.common.dto.TokenRequest;
import com.keli.common.dto.TokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "token-server")
public interface TokenClient {
    @PostMapping("/api/token/generateAccess")
    TokenResponse accessTokenGenerate(@RequestBody TokenRequest tokenRequest);
    @PostMapping("/api/token/generateRefresh")
    TokenResponse refreshTokenGenerate(@RequestBody TokenRequest tokenRequest);
}
