package com.keli.authlogin.common.exception.loginException;
// 账户锁定
public class AccountLockedException extends LoginException{
    public AccountLockedException(String message) {
        super("ACCOUNT_LOCKED", message);
    }
}
