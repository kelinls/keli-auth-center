package com.keli.authserver.dto.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthorizationGrantTypeDeserializer extends JsonDeserializer<AuthorizationGrantType> {
    // 静态常量缓存，优先返回预定义实例
    private static final Map<String, AuthorizationGrantType> CONSTANTS = new ConcurrentHashMap<>();

    static {
        // 注册所有预定义常量
        register(AuthorizationGrantType.AUTHORIZATION_CODE);
        register(AuthorizationGrantType.REFRESH_TOKEN);
        register(AuthorizationGrantType.CLIENT_CREDENTIALS);
        register(AuthorizationGrantType.PASSWORD);
        register(AuthorizationGrantType.JWT_BEARER);
        register(AuthorizationGrantType.DEVICE_CODE);
        register(AuthorizationGrantType.TOKEN_EXCHANGE);
    }

    private static void register(AuthorizationGrantType grantType) {
        CONSTANTS.put(grantType.getValue(), grantType);
    }

    @Override
    public AuthorizationGrantType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 确保当前 token 是字符串
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String value = p.getText();
            // 优先返回常量，否则创建新实例（也可以选择抛出异常，视需求而定）
            return CONSTANTS.getOrDefault(value, new AuthorizationGrantType(value));
        }
        if (p.currentToken() == JsonToken.START_OBJECT) {
            JsonNode node = p.readValueAsTree();
            if (node.has("value")) {
                String value = node.get("value").asText();
                return getOrCreate(value);
            }
        }

        throw new JsonParseException(p, "Unrecognized token");
    }
    private AuthorizationGrantType getOrCreate(String value) {
        return CONSTANTS.computeIfAbsent(value, AuthorizationGrantType::new);
    }
}
