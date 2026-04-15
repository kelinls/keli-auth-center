package com.keli.authlogin.dto;

import com.keli.authlogin.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginValidInfo {
    private User user;
    private String unverifiedPassword;
}
