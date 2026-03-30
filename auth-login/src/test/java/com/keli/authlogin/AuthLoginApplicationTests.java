package com.keli.authlogin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class AuthLoginApplicationTests {

    @Test
    void contextLoads() {
        String password = new BCryptPasswordEncoder().encode("kelinls");
        System.out.println(password);

    }

}
