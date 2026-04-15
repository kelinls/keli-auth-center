package com.keli.userinfoserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JwtDecoderConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    @Autowired
    private RestTemplate trustSelfSignedRestTemplate;

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // 使用 issuer-uri 进行 OIDC 发现
        return NimbusJwtDecoder.withIssuerLocation(issuerUri)
                .restOperations(trustSelfSignedRestTemplate)  // 注入自定义 RestTemplate
                .build();
    }
}
