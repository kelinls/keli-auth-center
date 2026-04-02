package com.keli.authlogin.feign;

import com.keli.common.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "session-server")
public interface SessionClient {
    @PostMapping("/api/session/createSession")
    String creatSession(UserInfo userInfo);
}
