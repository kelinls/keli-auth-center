package com.keli.authserver.dto.jackson;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.io.IOException;

public class OAuth2AccessTokenSerializer extends StdSerializer<OAuth2AccessToken> {
    public OAuth2AccessTokenSerializer() {
        super(OAuth2AccessToken.class);
    }

    @Override
    public void serialize(OAuth2AccessToken token, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("tokenValue", token.getTokenValue());
        gen.writeStringField("issuedAt", token.getIssuedAt().toString());
        gen.writeStringField("expiresAt", token.getExpiresAt().toString());
        gen.writeStringField("tokenType", token.getTokenType().getValue());
        gen.writeEndObject();
    }
    // 🔥 关键：官方缺失的方法，必须补上！
    @Override
    public void serializeWithType(OAuth2AccessToken value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {

    }
}
