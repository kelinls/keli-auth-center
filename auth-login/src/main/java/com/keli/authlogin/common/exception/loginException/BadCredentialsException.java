package com.keli.authlogin.common.exception.loginException;


//认证错误
public class BadCredentialsException extends LoginException{
    public BadCredentialsException(String message) {
        super("BAD_CREDENTIALS", message);
    }
}