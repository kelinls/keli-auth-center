package com.keli.authlogin;

import com.keli.authlogin.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AuthLoginApplicationTests {
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    UserMapper userMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Test
    void contextLoads() {
        ServiceInstance choose = loadBalancerClient.choose("session-server");
        System.out.println(choose.getUri());
    }

}
