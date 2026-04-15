package com.keli.authserver.dto.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

public abstract class AuthorizationGrantTypeMixin {
    @JsonCreator
    public static AuthorizationGrantType fromValue(@JsonProperty("value") String value) {
        // 注意：也可以直接使用 new AuthorizationGrantType(value)，但最好复用已有的常量
        // 这里为了简单，直接调用构造器；但 Spring Security 内部可能希望返回常量实例。
        // 更好的做法是：根据 value 返回对应的静态常量，避免重复创建。
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(value)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        }
        if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(value)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        }
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(value)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        }
        // 其他标准类型可以继续判断...
        return new AuthorizationGrantType(value);
    }
}
