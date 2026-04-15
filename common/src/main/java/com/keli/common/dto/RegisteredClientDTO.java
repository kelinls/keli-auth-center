package com.keli.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredClientDTO {
    private String id;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("client_authentication_methods")
    private Set<String> clientAuthenticationMethods;

    @JsonProperty("authorization_grant_types")
    private Set<String> authorizationGrantTypes;

    @JsonProperty("redirect_uris")
    private Set<String> redirectUris;

    @JsonProperty("post_logout_redirect_uris")
    private Set<String> postLogoutRedirectUris;

    private Set<String> scopes;

    @JsonProperty("client_settings")
    private Map<String, Object> clientSettings;

    @JsonProperty("token_settings")
    private Map<String, Object> tokenSettings;
}
