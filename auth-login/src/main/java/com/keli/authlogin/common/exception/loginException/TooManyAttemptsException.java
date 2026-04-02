package com.keli.authlogin.common.exception.loginException;
//尝试次数过多
public class TooManyAttemptsException extends LoginException{
    public TooManyAttemptsException(String message) {
        super("TOO_MANY_ATTEMPTS", message);
    }
}
