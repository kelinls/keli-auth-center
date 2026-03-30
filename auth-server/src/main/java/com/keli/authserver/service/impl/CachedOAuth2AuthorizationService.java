package com.keli.authserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CachedOAuth2AuthorizationService implements OAuth2AuthorizationService {
    private final String idCachePrefix = "auth:id:";
    private final String tokenCachePrefix = "auth:token:";
    private final boolean useCaffeine = true;
    private final RegisteredClientRepository registeredClientRepository;


    private final OAuth2AuthorizationService delegate; // 实际持久化的实现（如 JdbcOAuth2AuthorizationService）
    private final Cache<String, OAuth2Authorization> caffeineCache; // Caffeine 本地缓存
    private final RedisTemplate<String, OAuth2Authorization> redisTemplate;

    public CachedOAuth2AuthorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository,
                                            RedisTemplate<String, OAuth2Authorization> redisTemplate,
                                            ObjectMapper oauth2AuthorizationObjectMapper) {
        //对delegate添加objectMapper以允许对自定义的SsoSessionAuthenticationToken进行反序列化，原本的因为没有加入反序列化白名单，框架不允许
        JdbcOAuth2AuthorizationService jdbc =
                new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);

        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
        rowMapper.setObjectMapper(oauth2AuthorizationObjectMapper);
        jdbc.setAuthorizationRowMapper(rowMapper);
        this.delegate = jdbc;
        this.redisTemplate = redisTemplate;
        this.registeredClientRepository = registeredClientRepository;
        this.caffeineCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        String clientId = authorization.getRegisteredClientId();
        // 1. 保存到数据库（delegate 会持久化）
        delegate.save(authorization);
        // 2. 更新缓存
        cacheById(authorization, authorization.getId());
        // 按令牌值缓存（用于 findByToken）
        if(authorization.getAccessToken() != null) {
            cacheByToken(authorization, authorization.getAccessToken().getToken(),clientId);
        }
        if (authorization.getRefreshToken() != null) {
            cacheByToken(authorization, authorization.getRefreshToken().getToken(),clientId);
        }
        //获取authorization code，用auth code作为key来缓存auth对象
        //授权码在使用一次后会被清空，再调用save，此时这里获得为null
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authCode = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authCode != null) {
            cacheByToken(authorization,authCode.getToken(),clientId);
        }
    }
    private void cacheById(OAuth2Authorization authorization,String id) {
        if(id != null) {
            String idKey = idCachePrefix + authorization.getId();
            if(useCaffeine){
                caffeineCache.put(idKey, authorization);
            }
            redisTemplate.opsForValue().set(idKey, authorization, Duration.ofMinutes(30));
        }

    }

    private void cacheByToken(OAuth2Authorization authorization, AbstractOAuth2Token token,String clientId) {
        if (token != null) {
            String tokenKey = tokenCachePrefix + token.getTokenValue();
            System.out.println(clientId);
            RegisteredClient registeredClient = registeredClientRepository.findById(clientId);
            Duration accessTokenTimeToLive = null;
            if(registeredClient != null) {
                accessTokenTimeToLive = registeredClient.getTokenSettings().getAccessTokenTimeToLive();
            }
            assert accessTokenTimeToLive != null;
            if(useCaffeine){
                caffeineCache.put(tokenKey, authorization);
            }
            if(token instanceof OAuth2AuthorizationCode){
                redisTemplate.opsForValue().set(tokenKey, authorization, Duration.ofMinutes(5));
            }
            if(token instanceof OAuth2AccessToken){
                //这里设置和accessToken的过期时间一致
                redisTemplate.opsForValue().set(tokenKey, authorization,accessTokenTimeToLive);
            }
            if(token instanceof OAuth2RefreshToken){
                redisTemplate.opsForValue().set(tokenKey, authorization,Duration.ofMinutes(60));
            }


        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        // 1. 删除数据库记录
        delegate.remove(authorization);
        // 2. 清除缓存
        invalidIdCache(authorization.getId());
        // 清除令牌相关缓存
        if (authorization.getAccessToken() != null) {
            invalidTokenCache(authorization.getAccessToken().getToken().getTokenValue());
        }
        if (authorization.getRefreshToken() != null) {
           invalidTokenCache(authorization.getRefreshToken().getToken().getTokenValue());
        }
    }
    private void invalidIdCache(String id){
        if(id != null){
            String idKey = idCachePrefix + id;
            caffeineCache.invalidate(idKey);
            redisTemplate.delete(idKey);
        }
    }
    private void invalidTokenCache(String token){
        if(token != null){
            String tokenKey = tokenCachePrefix + token;
            redisTemplate.delete(tokenKey);
            caffeineCache.invalidate(tokenKey);
        }
    }

    @Override
    public OAuth2Authorization findById(String id) {
        String key = idCachePrefix + id;
        // 1. 从 Caffeine 获取
        OAuth2Authorization auth = null;
        if(useCaffeine){
             auth = caffeineCache.getIfPresent(key);

        }
        if (auth != null) {
            return auth;
        }
        // 2. 从 Redis 获取
        auth = redisTemplate.opsForValue().get(key);
        if (auth != null) {
            if(useCaffeine){
                caffeineCache.put(key, auth);
            }
            return auth;
        }
        // 3. 从数据库获取
        auth = delegate.findById(id);
        if (auth != null) {
            // 回填缓存
            cacheById(auth,id);
        }
        return auth;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        String key = tokenCachePrefix + token;
        // 1. 从 Caffeine 获取
        OAuth2Authorization auth = null;
        if(useCaffeine){
            auth = caffeineCache.getIfPresent(key);
        }
        if (auth != null) {
            return auth;
        }
        // 2. 从 Redis 获取
        auth = redisTemplate.opsForValue().get(key);
        if (auth != null) {
            if(useCaffeine){
                caffeineCache.put(key, auth);
            }
            return auth;
        }
        // 3. 从数据库获取
        auth = delegate.findByToken(token, tokenType);
        if (auth != null) {
            String clientId = auth.getRegisteredClientId();
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
            Duration accessTokenTimeToLive = null;
            if (registeredClient != null) {
                 accessTokenTimeToLive = registeredClient.getTokenSettings().getAccessTokenTimeToLive();
            }
            assert accessTokenTimeToLive != null;
            if(tokenType == OAuth2TokenType.ACCESS_TOKEN){
                redisTemplate.opsForValue().set(key, auth, accessTokenTimeToLive);
            }
            if(tokenType == OAuth2TokenType.REFRESH_TOKEN){
                redisTemplate.opsForValue().set(key, auth, Duration.ofMinutes(60));
            }
            //这里就不设置authorization code的了
            }
        return auth;
    }
}
