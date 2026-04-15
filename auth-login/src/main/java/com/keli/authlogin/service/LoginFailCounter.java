package com.keli.authlogin.service;

public interface LoginFailCounter {
    void countLoginFail(String uid,String remoteAddr);
    boolean loginMissValidate();
}
