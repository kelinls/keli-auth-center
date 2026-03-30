package com.keli.authlogin.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "session-server")
public interface SessionClient {

}
