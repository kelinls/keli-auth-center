package com.keli.clientserver.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keli.common.oauth2.settings.ConfigurationSettingNames;
import com.keli.common.oauth2.settings.RegisteredClientSettingsDefaults;
import com.keli.clientserver.entity.OauthClientSettings;
import com.keli.clientserver.entity.OauthTokenSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SettingsMapBuilder {
    private final ObjectMapper objectMapper;
    /**
     * 将数据库查询出的 client settings 实体转换为 Spring 所需的 Map
     */
    public Map<String, Object> buildClientSettingsMap(OauthClientSettings entity) {
        Map<String, Object> settings = RegisteredClientSettingsDefaults.copyClientDefaults();
        if (entity == null) {
            return settings;
        }

        // 基础字段映射（仅当非 null 时覆盖默认值）
        if (entity.getRequireProofKey() != null) {
            settings.put(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY, entity.getRequireProofKey() != 0);
        }
        if (entity.getRequireAuthorizationConsent() != null) {
            settings.put(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT, entity.getRequireAuthorizationConsent() != 0);
        }
        if (StringUtils.hasText(entity.getJwkSetUrl())) {
            settings.put(ConfigurationSettingNames.Client.JWK_SET_URL, entity.getJwkSetUrl());
        }
        if (StringUtils.hasText(entity.getTokenEndpointAuthenticationSigningAlgorithm())) {
            settings.put(ConfigurationSettingNames.Client.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM,
                    entity.getTokenEndpointAuthenticationSigningAlgorithm());
        }
        if (StringUtils.hasText(entity.getX509CertificateSubjectDn())) {
            settings.put(ConfigurationSettingNames.Client.X509_CERTIFICATE_SUBJECT_DN, entity.getX509CertificateSubjectDn());
        }

        // 合并 settings_json 中的扩展字段
        mergeJsonExtensions(settings, entity.getSettingsJson());

        return settings;
    }

    /**
     * 将数据库查询出的 token settings 实体转换为 Spring 所需的 Map
     */
    public Map<String, Object> buildTokenSettingsMap(OauthTokenSettings entity) {
        Map<String, Object> settings = RegisteredClientSettingsDefaults.copyTokenDefaults();
        if (entity == null) {
            return settings;
        }

        if (entity.getAccessTokenTtl() != null) {
            settings.put(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, entity.getAccessTokenTtl());
        }
        if (entity.getRefreshTokenTtl() != null) {
            settings.put(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, entity.getRefreshTokenTtl());
        }
        if (entity.getAuthorizationCodeTtl() != null) {
            settings.put(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE, entity.getAuthorizationCodeTtl());
        }
        if (entity.getDeviceCodeTtl() != null) {
            settings.put(ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE, entity.getDeviceCodeTtl());
        }
        if (entity.getReuseRefreshTokens() != null) {
            settings.put(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS, entity.getReuseRefreshTokens() != 0);
        }
        if (StringUtils.hasText(entity.getIdTokenSignatureAlgorithm())) {
            settings.put(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM, entity.getIdTokenSignatureAlgorithm());
        }

        // 合并扩展 JSON
        mergeJsonExtensions(settings, entity.getSettingsJson());

        return settings;
    }

    /**
     * 将 settings_json 字段中的 JSON 内容合并到现有 Map 中
     */
    private void mergeJsonExtensions(Map<String, Object> targetMap, String json) {
        if (!StringUtils.hasText(json)) {
            return;
        }
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});
            // 合并，注意如果 JSON 中有与列字段同名的键，会覆盖（可根据业务决定谁优先）
            targetMap.putAll(jsonMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse settings_json", e);
        }
    }
}
