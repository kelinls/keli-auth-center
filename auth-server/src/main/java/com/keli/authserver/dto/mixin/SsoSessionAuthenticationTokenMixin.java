package com.keli.authserver.dto.mixin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 与 {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken} 的 Mixin 类似：
 * 需配合目标类上的 {@link com.fasterxml.jackson.annotation.JsonCreator} 工厂方法，才能从 JDBC 中反序列化。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SsoSessionAuthenticationTokenMixin {
}
