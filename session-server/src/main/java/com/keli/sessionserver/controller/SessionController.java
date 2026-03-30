package com.keli.sessionserver.controller;

import com.keli.sessionserver.dto.UserInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @PostMapping("/validate")
    public UserInfo validateSession(@RequestParam String sessionId) {
        System.out.println("sessionId:"+sessionId);
        return new UserInfo(sessionId,"kelinls");
    }
}
