package com.keli.authlogin.common.exception.loginException;

//账户禁用
public class AccountDisabledException extends LoginException{
    public AccountDisabledException(String message) {
        super("ACCOUNT_DISABLED", message);
    }
}
