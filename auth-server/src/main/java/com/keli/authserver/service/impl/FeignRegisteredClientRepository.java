package com.keli.authserver.service.impl;

import com.keli.authserver.feign.ClientManagementFeignClient;
import com.keli.common.dto.RegisteredClientDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
@Component
@Primary
public class FeignRegisteredClientRepository implements RegisteredClientRepository {
    @Autowired
    private ClientManagementFeignClient feignClient;
    @Override
    public RegisteredClient findById(String id) {
        // 注意：id 通常是内部 ID，也可能是 clientId，看你的远程 API 设计
        RegisteredClientDTO dto = feignClient.getClientById(id);
        return convertToRegisteredClient(dto);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        RegisteredClientDTO dto = feignClient.getClientByClientId(clientId);
        return convertToRegisteredClient(dto);
    }
    private RegisteredClient convertToRegisteredClient(RegisteredClientDTO dto) {
        Set<String> clientAuthenticationMethods = dto.getClientAuthenticationMethods() != null ? dto.getClientAuthenticationMethods() : Set.of();
        Set<String> authorizationGrantTypes = dto.getAuthorizationGrantTypes() != null ? dto.getAuthorizationGrantTypes() : Set.of();
        Set<String> redirectUris = dto.getRedirectUris() != null ? dto.getRedirectUris() : Set.of();
        Set<String> postLogoutRedirectUris = dto.getPostLogoutRedirectUris() != null ? dto.getPostLogoutRedirectUris() : Set.of();
        Set<String> scopes = dto.getScopes() != null ? dto.getScopes() : Set.of();

        Map<String, Object> clientSettings = normalizeClientSettings(dto.getClientSettings());
        Map<String, Object> tokenSettings = normalizeTokenSettings(dto.getTokenSettings());

        // 使用 Spring Authorization Server 提供的 Builder 进行转换
        return RegisteredClient.withId(dto.getId())
                .clientId(dto.getClientId())
                .clientSecret(dto.getClientSecret())
                .clientAuthenticationMethods(methods ->
                        methods.addAll(clientAuthenticationMethods.stream()
                                .map(ClientAuthenticationMethod::new)
                                .collect(Collectors.toSet())))
                .authorizationGrantTypes(grantTypes ->
                        grantTypes.addAll(authorizationGrantTypes.stream()
                                .map(AuthorizationGrantType::new)
                                .collect(Collectors.toSet())))
                .redirectUris(uris -> uris.addAll(redirectUris))
                .postLogoutRedirectUris(uris -> uris.addAll(postLogoutRedirectUris))
                .scopes(s -> s.addAll(scopes))
                .clientSettings(ClientSettings.withSettings(clientSettings).build())
                .tokenSettings(TokenSettings.withSettings(tokenSettings).build())
                .build();
    }

    private Map<String, Object> normalizeClientSettings(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> settings = new HashMap<>(raw);
        settings.computeIfPresent(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY, (k, v) -> toBoolean(v));
        settings.computeIfPresent(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT, (k, v) -> toBoolean(v));
        return settings;
    }

    private Map<String, Object> normalizeTokenSettings(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> settings = new HashMap<>(raw);
        settings.computeIfPresent(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, (k, v) -> toDurationSeconds(v));
        settings.computeIfPresent(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, (k, v) -> toDurationSeconds(v));
        settings.computeIfPresent(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE, (k, v) -> toDurationSeconds(v));
        settings.computeIfPresent(ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE, (k, v) -> toDurationSeconds(v));
        settings.computeIfPresent(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS, (k, v) -> toBoolean(v));
        // client-server / JSON 中为字符串，TokenSettings 内部要求 OAuth2TokenFormat 类型
        settings.computeIfPresent(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT, (k, v) -> toAccessTokenFormat(v));
        settings.computeIfPresent(ConfigurationSettingNames.Token.X509_CERTIFICATE_BOUND_ACCESS_TOKENS, (k, v) -> toBoolean(v));
        // Feign/JSON 中为 "RS256" 字符串，TokenSettings 要求 SignatureAlgorithm
        settings.computeIfPresent(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM, (k, v) -> toIdTokenSignatureAlgorithm(v));
        return settings;
    }

    private static SignatureAlgorithm toIdTokenSignatureAlgorithm(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof SignatureAlgorithm algorithm) {
            return algorithm;
        }
        if (value instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) {
                return null;
            }
            return SignatureAlgorithm.valueOf(t.toUpperCase());
        }
        throw new IllegalArgumentException("Unsupported id token signature algorithm: " + value.getClass().getName());
    }

    /**
     * 将 Feign/JSON 中的 access token format 字符串还原为 SAS 所需的 {@link OAuth2TokenFormat}。
     */
    private static OAuth2TokenFormat toAccessTokenFormat(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof OAuth2TokenFormat format) {
            return format;
        }
        if (value instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) {
                return null;
            }
            if (OAuth2TokenFormat.SELF_CONTAINED.getValue().equalsIgnoreCase(t)) {
                return OAuth2TokenFormat.SELF_CONTAINED;
            }
            if (OAuth2TokenFormat.REFERENCE.getValue().equalsIgnoreCase(t)) {
                return OAuth2TokenFormat.REFERENCE;
            }
            throw new IllegalArgumentException("Unsupported access token format: " + t);
        }
        throw new IllegalArgumentException("Unsupported access token format type: " + value.getClass().getName());
    }

    private Duration toDurationSeconds(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Duration duration) {
            return duration;
        }
        if (value instanceof Number number) {
            return Duration.ofSeconds(number.longValue());
        }
        if (value instanceof String s) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            if (trimmed.startsWith("PT")) {
                return Duration.parse(trimmed);
            }
            return Duration.ofSeconds(Long.parseLong(trimmed));
        }
        throw new IllegalArgumentException("Unsupported duration value: " + value.getClass().getName());
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        if (value instanceof String s) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return "1".equals(trimmed) || "true".equalsIgnoreCase(trimmed);
        }
        throw new IllegalArgumentException("Unsupported boolean value: " + value.getClass().getName());
    }
    @Override
    public void save(RegisteredClient registeredClient) {

    }
}
