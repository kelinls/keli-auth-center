package com.keli.authserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

@Configuration
public class RedisConfig {

    /**
     * 必须与 JDBC 侧的 {@link org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService} 使用同一套 Jackson 模块。
     * <p>
     * 使用 {@link Jackson2JsonRedisSerializer} 并固定类型为 {@link OAuth2Authorization}，
     * 避免 {@link org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer} 在根上写 {@code @class}
     * 从而触发 {@code AllowlistTypeIdResolver} 拒绝 {@code OAuth2Authorization} 的问题。
     */
    @Bean
    public RedisTemplate<String, OAuth2Authorization> redisTemplate(
            RedisConnectionFactory connectionFactory,
            @Qualifier(JacksonConfig.OAUTH2_AUTHORIZATION_OBJECT_MAPPER) ObjectMapper oauth2AuthorizationObjectMapper) {
        ObjectMapper redisMapper = oauth2AuthorizationObjectMapper.copy();
        Jackson2JsonRedisSerializer<OAuth2Authorization> valueSerializer =
                new Jackson2JsonRedisSerializer<>(redisMapper, OAuth2Authorization.class);

        RedisTemplate<String, OAuth2Authorization> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        return template;
    }
}
