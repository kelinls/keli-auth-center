package com.keli.authserver.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keli.common.dto.SsoTokenCredentials;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SsoSessionAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    private final Object principal;

    private final Object credentials;

    //一个构造方法用来构造未认证的对象
    public SsoSessionAuthenticationToken(SsoTokenCredentials credentials) {
        super(null);
        this.principal = null;
        this.credentials = credentials;
        setAuthenticated(false);
    }
    public SsoSessionAuthenticationToken( Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        setAuthenticated(true);
    }

    /**
     * 供 Jackson 从 {@link org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService} 持久化 JSON 反序列化。
     * 字段需与 {@link AbstractAuthenticationToken} 的序列化形态兼容（authorities、details、authenticated、principal 等）。
     */
    @JsonCreator
    public static SsoSessionAuthenticationToken fromJackson(
            @JsonProperty("sSoTokenCredentials") SsoTokenCredentials ssoTokenCredentials,
            @JsonProperty("principal") Object principal,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities,
            @JsonProperty("details") Object details,
            @JsonProperty("authenticated") Boolean authenticated) {
        boolean auth = !Boolean.FALSE.equals(authenticated);
        SsoSessionAuthenticationToken token;
        if (auth) {
            token = new SsoSessionAuthenticationToken(
                    principal,
                    authorities != null ? authorities : List.of());
        } else {
            token = new SsoSessionAuthenticationToken(ssoTokenCredentials);
        }
        if (details != null) {
            token.setDetails(details);
        }
        return token;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
