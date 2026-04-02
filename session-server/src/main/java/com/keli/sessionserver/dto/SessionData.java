package com.keli.sessionserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionData {
    String uid;
    String username;
    List<String> roles;
    long createdAt;
    long lastAccessAt;
    long expiredAt;
    String clientIp;
    String userAgent;

}
