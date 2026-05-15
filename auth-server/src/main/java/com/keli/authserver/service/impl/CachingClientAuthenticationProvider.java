package com.keli.authserver.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CachingClientAuthenticationProvider implements AuthenticationProvider {
    private static final String SECRET_MATCH_CACHE_PREFIX = "cache:client:secret-match:";
    private static final Duration SECRET_MATCH_REDIS_TTL = Duration.ofMinutes(5);

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${keli.auth-server.client-secret-cache.use-caffeine:true}")
    private boolean useCaffeine;
    private final Cache<String, Boolean> secretMatchCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof OAuth2ClientAuthenticationToken clientAuthentication)) {
            return null;
        }
        ClientAuthenticationMethod authenticationMethod = clientAuthentication.getClientAuthenticationMethod();
        if (!ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(authenticationMethod)
                && !ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(authenticationMethod)) {
            return null;
        }
        if (!(clientAuthentication.getCredentials() instanceof String presentedSecret)
                || !StringUtils.hasText(presentedSecret)) {
            return null;
        }

        String clientId = clientAuthentication.getName();
        if (!StringUtils.hasText(clientId)) {
            return null;
        }
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null || !StringUtils.hasText(registeredClient.getClientSecret())) {
            return null;
        }

        String cacheKey = SECRET_MATCH_CACHE_PREFIX + clientId + ":" + sha256(presentedSecret);
        Boolean cacheMatched = getMatchByCache(cacheKey);
        boolean matched = cacheMatched != null
                ? cacheMatched
                : passwordEncoder.matches(presentedSecret, registeredClient.getClientSecret());
        if (cacheMatched == null) {
            cacheMatchResult(cacheKey, matched);
        }
        if (!matched) {
            return null;
        }

        OAuth2ClientAuthenticationToken authenticated = new OAuth2ClientAuthenticationToken(
                registeredClient,
                authenticationMethod,
                clientAuthentication.getCredentials()
        );
        authenticated.setDetails(clientAuthentication.getDetails());
        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Boolean getMatchByCache(String cacheKey) {
        if (useCaffeine) {
            Boolean caffeineMatched = secretMatchCache.getIfPresent(cacheKey);
            if (caffeineMatched != null) {
                return caffeineMatched;
            }
        }
        String redisValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (!StringUtils.hasText(redisValue)) {
            return null;
        }
        boolean matched = "1".equals(redisValue);
        if (useCaffeine) {
            secretMatchCache.put(cacheKey, matched);
        }
        return matched;
    }

    private void cacheMatchResult(String cacheKey, boolean matched) {
        String redisValue = matched ? "1" : "0";
        stringRedisTemplate.opsForValue().set(cacheKey, redisValue, SECRET_MATCH_REDIS_TTL);
        if (useCaffeine) {
            secretMatchCache.put(cacheKey, matched);
        }
    }

    private String sha256(String rawSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawSecret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not found", ex);
        }
    }
}
