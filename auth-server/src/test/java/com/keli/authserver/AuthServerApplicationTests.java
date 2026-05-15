package com.keli.authserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AuthServerApplicationTests {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() throws JsonProcessingException {
        long time1 = System.currentTimeMillis();
        boolean matches = passwordEncoder.matches("kellinls", "$2a$10$WwGZ2PEtVNgIT5MBV/2KvOE3F6d101WxScw6VtS1T.MWGrh64NClO");
        long time2 = System.currentTimeMillis();
        System.out.println((time2 - time1) + "ms");
        System.out.println(matches);
    }

}
