package com.keli.authlogin.controller;


import com.keli.authlogin.common.utils.R;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
@Slf4j
public class LoginController {
    @PostMapping("/process")
    public R loginByPwd(){
        return R.success(null);
    }
    @PostMapping("/register")
    public R register(){
        return R.success(null);
    }
    @GetMapping("/logout")
    public R logout(){
        return R.success(null);
    }
    @GetMapping("/callback")
    public R authCallback(String code, String state){
        log.info("进行了回调 code+{},state:{}", code, state);
        return R.success(null);
    }

}
