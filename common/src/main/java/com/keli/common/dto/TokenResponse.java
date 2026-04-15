package com.keli.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponse {
    /**
     * 令牌值
     */
    @JsonProperty("token_value")
    private String tokenValue;

    private Map<String,Object> headers;

    /**
     * 访问令牌有效期（秒）
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * 签发时间戳（毫秒或秒，根据实现决定）
     */
    @JsonProperty("issued_at")
    private Long issuedAt;

    /**
     * 过期时间戳（毫秒或秒）
     */
    @JsonProperty("expires_at")
    private Long expiresAt;

    /**
     * 授权记录标识，应原样返回，用于授权服务器存储时定位
     */
    @JsonProperty("authorization_id")
    private String authorizationId;

    /**
     * 令牌类型，标识access和refresh
     */
    @JsonProperty("token_type")
    private String tokenType;
}
