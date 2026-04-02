package com.keli.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SsoTokenCredentials {
    private String sessionId;
    private String clientIp;
    private String userAgent;
}
