package com.keli.authlogin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.keli.authlogin.mapper")
@EnableDiscoveryClient
public class AuthLoginApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthLoginApplication.class, args);
    }
}
