package com.keli.sessionserver.common.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class SessionIdGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SESSION_ID_BYTES = 32; // 256 bits

    public static String generateSessionId() {
        byte[] bytes = new byte[SESSION_ID_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        // 使用 URL 安全的 Base64 编码（去掉末尾填充）
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
