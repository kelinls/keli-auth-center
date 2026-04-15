package com.keli.authlogin.common.emun;

public enum UserStatus {
    ACTIVE(0, "活跃"),
    DISABLED(1, "禁用");

    private final int code;
    private final String description;

    UserStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
