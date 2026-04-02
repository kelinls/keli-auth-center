package com.keli.authserver.config;


import com.keli.authserver.feign.SessionClient;
import com.keli.authserver.filter.SsoSessionAuthenticationFilter;
import com.keli.authserver.service.impl.CustomRefreshTokenAuthenticationProvider;
import com.keli.authserver.service.impl.ExternalLoginAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;


@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * token-service 上公开的 JWKS 绝对 URL，写入
     * {@link org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationServerMetadata} 的 {@code jwks_uri}。
     */
    @Value("${keli.token-service.jwks-uri:http://localhost:8082/oauth2/jwks}")
    private String tokenServiceJwksUri;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private SessionClient sessionClient;
    @Autowired
    private CustomRefreshTokenAuthenticationProvider customRefreshTokenAuthenticationProvider;
@Bean
public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(HttpSecurity http) {
    // 注意：这里传入的 http 是尚未构建的实例，但 lambda 会在请求时执行，此时 http 已构建好
    return request -> http.getSharedObject(AuthenticationManager.class);
}
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        SsoSessionAuthenticationFilter ssoFilter = new SsoSessionAuthenticationFilter(authenticationManager,sessionClient);
        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) -> {
                    authorizationServer.authorizationServerMetadataEndpoint(metadata ->
                            metadata.authorizationServerMetadataCustomizer(builder ->
                                    builder.jwkSetUrl(tokenServiceJwksUri)));
                })
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .anyRequest().authenticated()
                )
                //框架通过authenticationManager中的provider来进行认证，但是，authenticationManager有不止一个，我们要自定义认证流程中使用到manager的话，一定要注意我们使用的是哪个manager，对于自定义内容还是推荐用自定义manager，防止其他manager里没有我们自定义的provider
                //注意ssoFilter在过滤器链中的顺序，这个很重要，顺序不对会出稀奇古怪的bug，之前第一次登录必定跳/error就是因为这个原因
                .addFilterAfter(ssoFilter, SecurityContextHolderFilter.class)
                .authenticationProvider(customRefreshTokenAuthenticationProvider)
                .csrf(AbstractHttpConfigurer::disable)
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new ExternalLoginAuthenticationEntryPoint("http://localhost:8080/api/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        // 放行 Swagger UI 和 OpenAPI 文档路径
                        .requestMatchers(
                                "/swagger-ui/**",      // Swagger UI 资源
                                "/swagger-ui.html",    // Swagger UI 入口页面
                                "/api-docs/**"      // OpenAPI 文档 JSON
                        ).permitAll()
                        // 其他请求需要认证
                        .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
