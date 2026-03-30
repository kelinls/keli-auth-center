package com.keli.authserver.feign;

import com.keli.authserver.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "session-server")
public interface SessionClient {
    //一定要加上@RequestParam注解，这样才能让openFeign从参数中获取这个简单对象
    @PostMapping("/api/session/validate")
    UserInfo validateSession(@RequestParam String sessionId);
}
