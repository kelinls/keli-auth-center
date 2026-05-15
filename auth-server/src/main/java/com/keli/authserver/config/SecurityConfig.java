package com.keli.authserver.config;


import com.keli.authserver.feign.SessionClient;
import com.keli.authserver.filter.SsoSessionAuthenticationFilter;
import com.keli.authserver.oidc.ClientScopedOidcLogoutSuccessHandler;
import com.keli.authserver.oidc.CustomOidcUserInfoMapper;
import com.keli.authserver.service.impl.CachingClientAuthenticationProvider;
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
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * token-service 上公开的 JWKS 绝对 URL，写入
     * {@link org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationServerMetadata} 的 {@code jwks_uri}。
     */
    @Value("${keli.auth-server.login-uri}")
    private String loginUri;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private SessionClient sessionClient;
    @Autowired
    private SessionRegistry sessionRegistry;
    @Autowired
    private CustomRefreshTokenAuthenticationProvider customRefreshTokenAuthenticationProvider;
    @Autowired
    private ClientScopedOidcLogoutSuccessHandler clientScopedOidcLogoutSuccessHandler;
    @Autowired
    private CachingClientAuthenticationProvider cachingClientAuthenticationProvider;
@Bean
public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(HttpSecurity http) {
    // 注意：这里传入的 http 是尚未构建的实例，但 lambda 会在请求时执行，此时 http 已构建好
    return request -> http.getSharedObject(AuthenticationManager.class);
}
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder)
            throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        SsoSessionAuthenticationFilter ssoFilter = new SsoSessionAuthenticationFilter(
                authenticationManager,
                sessionClient,
                sessionRegistry
        );
        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) -> {
                    authorizationServer.oidc(oidcConfigurer -> oidcConfigurer
                            .userInfoEndpoint(userinfoConfigurer ->
                                    userinfoConfigurer.userInfoMapper(CustomOidcUserInfoMapper::getOidcUserInfo))
                            .logoutEndpoint(logout -> logout
                                    .logoutResponseHandler(clientScopedOidcLogoutSuccessHandler))
                    )
                            .clientAuthentication(auth->{
                                auth.authenticationProviders(providers->{
                                    providers.addFirst(cachingClientAuthenticationProvider);
                                });
                            });
                })
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .anyRequest().authenticated()
                )
                // 让授权服务器端点（尤其是 OIDC /userinfo）也能通过 Bearer Token 完成认证，
                // 否则请求会落到 anonymous，最终在 AuthorizationFilter 被拒绝。
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                //框架通过authenticationManager中的provider来进行认证，但是，authenticationManager有不止一个，我们要自定义认证流程中使用到manager的话，一定要注意我们使用的是哪个manager，对于自定义内容还是推荐用自定义manager，防止其他manager里没有我们自定义的provider
                //注意ssoFilter在过滤器链中的顺序，这个很重要，顺序不对会出稀奇古怪的bug，之前第一次登录必定跳/error就是因为这个原因
                .addFilterAfter(ssoFilter, SecurityContextHolderFilter.class)
                .authenticationProvider(customRefreshTokenAuthenticationProvider)
                .csrf(AbstractHttpConfigurer::disable)
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new ExternalLoginAuthenticationEntryPoint(loginUri),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );
        return http.build();
    }

    //授权服务器配置认证，为/userinfo端口提供认证
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("SCOPE_"); // 默认前缀是 "SCOPE_"，可根据需要修改

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            //这里使用了上面的默认转换器，获得了scope中的权限
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            // 假设你的 JWT 中包含一个名为 "roles" 的数组
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                authorities.addAll(roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList());
            }
            System.out.println(Arrays.toString(authorities.toArray()));
            return authorities;
        });
        return jwtConverter;
    }
    //权限继承
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_super_admin > ROLE_admin\nROLE_admin > ROLE_user");
        return roleHierarchy;
    }
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }


}
