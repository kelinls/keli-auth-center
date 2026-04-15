package com.keli.authserver.utils;

import com.keli.authserver.dto.SsoSessionAuthenticationToken;
import com.keli.common.dto.SsoSessionPrincipal;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

public class PrincipalExtractUtil {
    public static SsoSessionPrincipal getPrincipal(OAuth2Authorization authorization) {
        if (authorization == null) {
            return null;
        }
        SsoSessionAuthenticationToken ssoSessionAuthenticationToken = authorization.getAttribute("java.security.Principal");
        SsoSessionPrincipal principal = null;
        if (ssoSessionAuthenticationToken != null) {
            principal = (SsoSessionPrincipal) ssoSessionAuthenticationToken.getPrincipal();
        }
        return principal;
    }
}
