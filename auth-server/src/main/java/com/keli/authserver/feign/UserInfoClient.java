package com.keli.authserver.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "userinfo-server")
public interface UserInfoClient {
    @GetMapping("/internal/user/queryUserInfoById")
    Map<String,Object> queryUserInfoById(@RequestParam String uid);
}
