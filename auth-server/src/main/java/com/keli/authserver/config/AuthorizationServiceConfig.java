package com.keli.authserver.config;

import com.keli.authserver.service.impl.SsoSessionAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

@Slf4j
@Configuration
public class AuthorizationServiceConfig {


    @Value("${keli.auth-server.issuer}")
    private String issuer;


//@Bean
//public RegisteredClientRepository registeredClientRepository() {
////        String rawPwd = "kelinls";
////        String encode = passwordEncoder().encode(rawPwd);
//    RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
//            .clientId("keli-test-client3")
//            .clientSecret("{noop}kelinls4")
//            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
//            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//            //一定要确保这里的重定向uri和请求的时携带的一致，不然会直接报错跳转/error
//            .redirectUri("https://localhost:8080/api/auth/callback")
//            .postLogoutRedirectUri("https://localhost:8080/")
//            .scope("read")
//            .scope("write")
//            //.scope("openid")
//            .scope("client_server")
//            .clientSettings(ClientSettings.builder()
//                    .requireAuthorizationConsent(true).build())
//            .tokenSettings(TokenSettings.builder()
//                    .accessTokenTimeToLive(Duration.ofMinutes(10))
//                    .refreshTokenTimeToLive(Duration.ofDays(7)).build())
//            .build();
//
//
//    return new InMemoryRegisteredClientRepository(oidcClient);
//}


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
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    //可以替换为openFeign的，使用openFeign获取然后返回，需要自行构建JWKSource
//    @Bean
//    public JWKSource<SecurityContext> jwkSource() throws MalformedURLException {
//        URL jwkSetUrl = new URL(jwksUri);
//        return JWKSourceBuilder
//                .create(jwkSetUrl)
//                .retrying(true) // 开启重试机制
//                .build();
//    }
}
