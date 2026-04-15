package com.keli.authserver.dto;

import com.keli.authserver.feign.TokenClient;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CachedJwkSource implements JWKSource<SecurityContext> {
    @Autowired
    private TokenClient tokenClient;
    private volatile JWKSet cached;
    private volatile long lastUpdate;
    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext securityContext) throws KeySourceException {
        JWKSet jwkSet = null;
        try {
            jwkSet = getJWKSet();
        } catch (ParseException e) {
            log.error("获取jwk错误");
            throw new RuntimeException(e);
        }
        return  jwkSelector.select(jwkSet);
    }
    private JWKSet getJWKSet() throws ParseException {
        long now = System.currentTimeMillis();
        // 每 5 分钟刷新一次
        if (cached == null || (now - lastUpdate) > 300_000) {
            synchronized (this) {
                if (cached == null || (now - lastUpdate) > 300_000) {
                    Map<String,Object> jwkMap =  tokenClient.jwksEndpoint();
                    cached = JWKSet.parse(jwkMap);
                    lastUpdate = now;
                }
            }
        }
        return cached;
    }
}