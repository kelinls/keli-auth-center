package com.keli.authlogin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AuthLoginApplicationTests {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Test
    void contextLoads() {
        String encode = passwordEncoder.encode("123456");
        System.out.println(encode);
        System.out.println(passwordEncoder.matches("123456",encode));
    }
}
