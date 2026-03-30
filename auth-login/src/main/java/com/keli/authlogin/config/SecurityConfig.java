package com.keli.authlogin.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        // 放行 Swagger UI 和 OpenAPI 文档路径
//                        .requestMatchers(
//                                "/swagger-ui/**",      // Swagger UI 资源
//                                "/swagger-ui.html",    // Swagger UI 入口页面
//                                "/api-docs/**"      // OpenAPI 文档 JSON
//                        ).permitAll()
//                        // 其他请求需要认证
//                        .anyRequest().authenticated()
//                );
//        return http.build();
//    }
/*//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }*/
}
