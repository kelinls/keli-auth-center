package com.keli.authlogin.service.impl;

import com.keli.authlogin.common.emun.UserStatus;
import com.keli.authlogin.common.exception.loginException.AccountDisabledException;
import com.keli.authlogin.common.exception.loginException.BadCredentialsException;
import com.keli.authlogin.common.exception.loginException.TooManyAttemptsException;
import com.keli.authlogin.dto.LoginValidInfo;
import com.keli.authlogin.entity.User;
import com.keli.authlogin.service.LoginFailCounter;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserSecurityValidator {
    private final User user;
    private final LoginValidInfo loginValidInfo;
    private final PasswordEncoder passwordEncoder;
    private final LoginFailCounter loginFailCounter;
    public UserSecurityValidator(LoginValidInfo loginValidInfo, PasswordEncoder passwordEncoder, LoginFailCounter loginFailCounter) {
        this.user = loginValidInfo.getUser();
        this.loginValidInfo = loginValidInfo;
        this.passwordEncoder = passwordEncoder;
        this.loginFailCounter = loginFailCounter;
    }

    public void validate() {
        if(user.getStatus() == UserStatus.DISABLED.getCode()){
            throw new AccountDisabledException("用户已被封禁");
        }
        if(!loginFailCounter.loginMissValidate()){
            throw new TooManyAttemptsException("尝试错误次数过多");
        }
        if(!passwordValidate()){
            throw new BadCredentialsException("密码错误");
        }
    }

    private boolean passwordValidate(){
        return passwordEncoder.matches(loginValidInfo.getUnverifiedPassword(),  user.getPassword());
    }


}
