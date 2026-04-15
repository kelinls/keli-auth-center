package com.keli.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenRequest {
    /**
     * 客户端标识
     */
    @JsonProperty("client_id")
    private String clientId;

    /**
     * 资源所有者唯一标识（用户名或用户ID）
     */
    private String subject;

    /**
     * 授权范围，空格分隔的字符串
     */
    private String scope;

    /**
     * 用户的权限
     */
    private List<String> roles;

    /**
     * 令牌类型：access_token 或 refresh_token
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * 访问令牌有效期（秒）
     */
    @JsonProperty("access_token_ttl")
    private Long accessTokenTtl;

    /**
     * 刷新令牌有效期（秒），仅当 token_type = refresh_token 时可能需要
     */
    @JsonProperty("refresh_token_ttl")
    private Long refreshTokenTtl;

    /**
     * 令牌颁发者，通常为授权服务器的 issuer URL
     */
    private String issuer;

    /**
     * 授权记录的唯一标识，用于关联存储
     */
    @JsonProperty("authorization_id")
    private String authorizationId;

    /**
     * 附加声明，如 tenant_id 等自定义字段
     */
    @JsonProperty("additional_claims")
    private Map<String, Object> additionalClaims;

    /**
     * 前一个刷新令牌值（用于支持刷新令牌轮换时的验证）
     */
    @JsonProperty("previous_refresh_token")
    private String previousRefreshToken;
}
