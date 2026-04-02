package com.keli.authlogin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.keli.authlogin.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class AuthLoginApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthLoginApplication.class, args);
    }
}
