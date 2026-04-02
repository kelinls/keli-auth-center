package com.keli.authlogin.common.exception.loginException;

//验证码过期
public class CaptchaExpiredException extends LoginException{
    public CaptchaExpiredException(String message) {
        super("CAPTCHA_EXPIRED", message);
    }
}
