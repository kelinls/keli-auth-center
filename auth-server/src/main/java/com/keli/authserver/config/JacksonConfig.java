package com.keli.authserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keli.authserver.dto.SsoSessionAuthenticationToken;
import com.keli.authserver.dto.mixin.SsoSessionAuthenticationTokenMixin;
import com.keli.authserver.dto.mixin.SsoSessionPrincipalMixin;
import com.keli.authserver.service.impl.CachedOAuth2AuthorizationService;
import com.keli.common.dto.SsoSessionPrincipal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

@Configuration
public class JacksonConfig {

    public static final String OAUTH2_AUTHORIZATION_OBJECT_MAPPER = "oauth2AuthorizationObjectMapper";

    /**
     * Feign、Controller 等使用的默认 ObjectMapper。必须与 OAuth2 JDBC 专用 Mapper 分离，
     * 否则 Security 模块 / Mixin（如要求 {@code @class}）会破坏普通 JSON 反序列化。
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false).build();
    }

    /**
     * 仅用于 {@link JdbcOAuth2AuthorizationService}：持久化/读取 OAuth2Authorization（自定义 Authentication、嵌套 Principal）。
     */
    @Bean(name = OAUTH2_AUTHORIZATION_OBJECT_MAPPER)
    public ObjectMapper oauth2AuthorizationObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        objectMapper.registerModules(SecurityJackson2Modules.getModules(classLoader));
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        objectMapper.addMixIn(SsoSessionAuthenticationToken.class, SsoSessionAuthenticationTokenMixin.class);
        // principal 为 UserInfo 时必须注册 Mixin，否则 AllowlistTypeIdResolver 拒绝反序列化（仅作用于本 ObjectMapper，不影响 Feign）
        objectMapper.addMixIn(SsoSessionPrincipal.class, SsoSessionPrincipalMixin.class);
        return objectMapper;
    }

    @Bean
    @Primary
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository,
            RedisTemplate<String, OAuth2Authorization> redisTemplate,
            @Qualifier(OAUTH2_AUTHORIZATION_OBJECT_MAPPER) ObjectMapper oauth2AuthorizationObjectMapper) {
        return new CachedOAuth2AuthorizationService(
                jdbcTemplate, registeredClientRepository, redisTemplate, oauth2AuthorizationObjectMapper);
    }
}
