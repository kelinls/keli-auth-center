package com.keli.authserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class RedirectController {
    @GetMapping("/confirm")
    public String confirm(){
        return "confirm_access";
    }
}
