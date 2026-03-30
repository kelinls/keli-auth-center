package com.keli.tokenserver.controller;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 对外暴露 JWK Set（仅公钥），供资源服务器 / 客户端从 {@code jwks_uri} 拉取验签 JWT。
 * 必须与 {@link com.keli.tokenserver.config.JWKConfig} 中签发 JWT 使用的密钥一致。
 */
@RestController
public class JwkSetController {

    private final JWKSet jwkSet;

    public JwkSetController(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    @GetMapping(value = "/oauth2/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwkSetDocument() {
        return jwkSet.toPublicJWKSet().toJSONObject();
    }
}
