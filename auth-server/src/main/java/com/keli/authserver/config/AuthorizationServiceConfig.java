package com.keli.authserver.config;

import com.keli.authserver.service.impl.SsoSessionAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Configuration
public class AuthorizationServiceConfig {

    @Value("${keli.auth-server.issuer:http://localhost:9000}")
    private String issuer;

@Bean
public RegisteredClientRepository registeredClientRepository() {
//        String rawPwd = "kelinls";
//        String encode = passwordEncoder().encode(rawPwd);
    RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("keli-client")
            .clientSecret("{noop}kelinls")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:8080/api/login/callback")
            .postLogoutRedirectUri("http://localhost:8080/")
            .scope("read")
            .scope("write")
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(5))
                    .refreshTokenTimeToLive(Duration.ofDays(7)).build())
            .build();
    log.info("uuid:{}",oidcClient.getId());
    log.info("clientId:{}",oidcClient.getClientId());
    log.info("clientSecret:{}",oidcClient.getClientSecret());

    return new InMemoryRegisteredClientRepository(oidcClient);
}

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(SsoSessionAuthenticationProvider customProvider) {
        return new ProviderManager(customProvider);
    }

}
