package com.keli.authserver.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.keli.authserver.dto.jackson.AuthorizationGrantTypeDeserializer;
import com.keli.authserver.dto.jackson.OAuth2AuthorizationDeserializer;
import com.keli.authserver.dto.mixin.*;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

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
            RedisConnectionFactory redisConnectionFactory,RegisteredClientRepository registeredClientRepository,AutowireCapableBeanFactory beanFactory) {
        RedisTemplate<String, OAuth2Authorization> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Key 序列化 (String)
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);

        // Value 序列化 (JSON)
        RedisSerializer<OAuth2Authorization> valueSerializer = jacksonRedisSerializer(registeredClientRepository,beanFactory);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    /**
     * 专门用于 OAuth2Authorization 的 JSON 序列化器
     */
    @Bean
    public RedisSerializer<OAuth2Authorization> jacksonRedisSerializer(RegisteredClientRepository registeredClientRepository, AutowireCapableBeanFactory beanFactory ) {
        ObjectMapper objectMapper = new ObjectMapper();

//        // 必须：忽略未知字段（accessToken、refreshToken 等）
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);


        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        objectMapper.registerModules(SecurityJackson2Modules.getModules(classLoader));
        // 1. 注册 Security 官方提供的 Mixin 模块（解决核心类无构造器问题）
        objectMapper.registerModule(new CoreJackson2Module());
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        // 2. 强制添加手动反序列化（防止模块不生效的兜底）
        SimpleModule fixModule = new SimpleModule();
        fixModule.addDeserializer(AuthorizationGrantType.class, new AuthorizationGrantTypeDeserializer());
        fixModule.addDeserializer(OAuth2Authorization.class,new OAuth2AuthorizationDeserializer(registeredClientRepository));
        objectMapper.registerModule(fixModule);
        objectMapper.addMixIn(OAuth2AccessToken.class, OAuth2AccessTokenMixin.class);
        objectMapper.addMixIn(OAuth2Authorization.class, OAuth2AuthorizationMixin.class);
        objectMapper.addMixIn(OAuth2RefreshToken.class, OAuth2RefreshTokenMixin.class);
        objectMapper.addMixIn(OAuth2Authorization.Token.class, TokenMixin.class);
        objectMapper.addMixIn(OAuth2AccessToken.TokenType.class, TokenTypeMixin.class);
        objectMapper.addMixIn(OAuth2AuthorizationCode.class, OAuth2AuthorizationCodeMixin.class);
        objectMapper.addMixIn(OidcIdToken.class, OidcIdTokenMixin.class);
       // objectMapper.setHandlerInstantiator(new SpringHandlerInstantiator(beanFactory));
       // objectMapper.addMixIn(OAuth2Authorization.class, OAuth2AuthorizationMixin.class);
//        // 2. 启用多态类型识别（必须！否则反序列化会丢失类型信息）

        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

//        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
//                .allowIfSubType("org.springframework.security.oauth2.core.")
//                .allowIfSubType("org.springframework.security.oauth2.server.authorization.")
//                .allowIfSubType("com.keli.authserver.dto.")
//                .allowIfSubType("com.keli.common.dto.")
//                .allowIfSubType("java.util.")   // 允许 java.util 包下的类（如 UnmodifiableMap, ArrayList 等）
//                .allowIfSubType("java.lang.")
//                .build();
//        objectMapper.activateDefaultTyping(ptv);
        return new Jackson2JsonRedisSerializer<>(objectMapper,OAuth2Authorization.class);
    }
}
