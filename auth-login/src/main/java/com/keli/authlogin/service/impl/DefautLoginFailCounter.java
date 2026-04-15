package com.keli.authlogin.service.impl;

import com.keli.authlogin.service.LoginFailCounter;
import org.springframework.stereotype.Service;

@Service
public class DefautLoginFailCounter implements LoginFailCounter {
    @Override
    public void countLoginFail(String uid,String remoteAddr) {

    }

    @Override
    public boolean loginMissValidate() {
        return true;
    }
}
