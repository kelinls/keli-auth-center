package com.keli.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsoSessionPrincipal {
    String sessionId;
    String username;
    String uid;
    List<String> roles;
}
