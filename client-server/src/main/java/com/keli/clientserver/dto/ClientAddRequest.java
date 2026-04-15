package com.keli.clientserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientAddRequest {
    private String clientId;
    private String clientSecret;
    private String clientName;
    private String description;
    /**
     * 1: 启用, 0: 禁用
     */
    private Integer status;

    /**
     * OAuth2 ClientSettings（是否需要确认页等配置在这里）
     */
    private ClientSettingRequest clientSetting;

    /**
     * OAuth2 TokenSettings（access/refresh token 有效期等配置在这里）
     */
    private TokenSettingRequest tokenSetting;

    private List<String> scopes;
    private List<String> grantTypes;
    private List<String> redirectUris;
    private List<String> authMethods;
    private List<String> logoutUris;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClientSettingRequest {
        private Integer requireProofKey;
        private Integer requireAuthorizationConsent;
        private String jwkSetUrl;
        private String tokenEndpointAuthenticationSigningAlgorithm;
        private String x509CertificateSubjectDn;
        /**
         * 额外扩展字段，会序列化进 oauth_client_settings.settings_json
         */
        private Map<String, Object> extensions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenSettingRequest {
        /**
         * 秒
         */
        private Long accessTokenTtl;
        /**
         * 秒
         */
        private Long refreshTokenTtl;
        private Long authorizationCodeTtl;
        private Long deviceCodeTtl;
        /**
         * 1/0
         */
        private Integer reuseRefreshTokens;
        private String idTokenSignatureAlgorithm;
        /**
         * 额外扩展字段，会序列化进 oauth_token_settings.settings_json
         */
        private Map<String, Object> extensions;
    }
}
