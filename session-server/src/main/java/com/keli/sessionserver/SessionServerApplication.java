package com.keli.sessionserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SessionServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SessionServerApplication.class, args);
    }

}
