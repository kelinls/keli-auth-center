package com.keli.authserver.config;

import com.keli.authserver.feign.TokenClient;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.text.ParseException;
import java.util.Map;

@Configuration
public class JwtDecoderConfig {
    @Bean
    @Primary
    public JwtDecoder jwtDecoder(TokenClient tokenClient) throws ParseException, JOSEException {
        Map<String, Object> JwkMap = tokenClient.jwksEndpoint();
        JWKSet jwkSet = JWKSet.parse(JwkMap);
        JWK jwk = jwkSet.getKeyByKeyId("kelinls-kid");

        return NimbusJwtDecoder.withPublicKey( jwk.toRSAKey().toRSAPublicKey())
                .build();
    }
}
