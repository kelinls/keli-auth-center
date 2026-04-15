package com.keli.userinfoserver.controller;

import com.keli.userinfoserver.dto.InternalUserCreateRequest;
import com.keli.userinfoserver.dto.InternalUserDto;
import com.keli.userinfoserver.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/user")
public class InternalController {
    private final UserService userService;

    public InternalController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("queryUserInfoById")
    public Map<String,Object> queryUserInfoById(@RequestParam String uid){
        return userService.queryUserInfoById(uid);
    }
    @GetMapping("/queryByUsername")
    public InternalUserDto queryByUsername(@RequestParam String username) {
        return userService.queryForLoginByUsername(username);
    }

    @GetMapping("/queryByPhone")
    public InternalUserDto queryByPhone(@RequestParam String phone) {
        return userService.queryForLoginByPhone(phone);
    }

    @GetMapping("/queryByEmail")
    public InternalUserDto queryByEmail(@RequestParam String email) {
        return userService.queryForLoginByEmail(email);
    }

    @PostMapping("/create")
    public InternalUserDto createInternal(@RequestBody InternalUserCreateRequest request) {
        return userService.createInternalUser(request);
    }
}
