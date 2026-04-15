package com.keli.authserver.oidc;

import com.keli.authserver.feign.UserInfoClient;
import com.keli.authserver.utils.PrincipalExtractUtil;
import com.keli.common.dto.SsoSessionPrincipal;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomOidcUserInfoMapper {
    @Autowired
    private UserInfoClient userInfoClient;
    private static UserInfoClient staticUserinfoClient;
    @PostConstruct
    public void init() {
        staticUserinfoClient = this.userInfoClient;
    }
    public static OidcUserInfo getOidcUserInfo(OidcUserInfoAuthenticationContext context) {
        OAuth2Authorization authorization = context.getAuthorization();
        SsoSessionPrincipal principal = PrincipalExtractUtil.getPrincipal(authorization);
        String uid = principal != null ? principal.getUid() : null;
        Map<String, Object> userInfoMap = uid != null ? staticUserinfoClient.queryUserInfoById(uid) : null;
        Map<String, Object> normalized = new LinkedHashMap<>();
        if (userInfoMap != null) {
            normalized.putAll(userInfoMap);
        }
        // OIDC userinfo 必须包含 sub，且应与 id_token 的 sub 保持一致
        normalized.put(StandardClaimNames.SUB, uid);
        if (!normalized.containsKey("roles") && principal != null && principal.getRoles() != null) {
            normalized.put("roles", principal.getRoles());
        }
        return new OidcUserInfo(normalized);
    }
}
