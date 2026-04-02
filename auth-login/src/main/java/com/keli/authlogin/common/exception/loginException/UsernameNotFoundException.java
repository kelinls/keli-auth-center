package com.keli.authlogin.common.exception.loginException;

//未找到用户
public class UsernameNotFoundException extends LoginException{
    public UsernameNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }
}
