package com.keli.authlogin.common.exception.loginException;

//登录错误父类
public class LoginException extends RuntimeException{
    private final String errorCode;

    public LoginException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
