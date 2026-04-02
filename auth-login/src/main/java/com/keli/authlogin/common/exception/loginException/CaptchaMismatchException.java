package com.keli.authlogin.common.exception.loginException;

//验证码错误
public class CaptchaMismatchException extends LoginException{
    public CaptchaMismatchException(String message) {
        super("CAPTCHA_MISMATCH", message);
    }
}
