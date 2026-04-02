package com.keli.authlogin.controller;


import com.keli.authlogin.common.exception.loginException.BadCredentialsException;
import com.keli.authlogin.common.exception.loginException.UsernameNotFoundException;
import com.keli.authlogin.common.utils.FormatValidator;
import com.keli.authlogin.dto.LoginPasswordRequest;
import com.keli.authlogin.entity.Role;
import com.keli.authlogin.entity.User;
import com.keli.authlogin.feign.SessionClient;
import com.keli.authlogin.mapper.UserMapper;
import com.keli.common.dto.UserInfo;
import com.keli.common.utils.R;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class LoginController {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SessionClient sessionClient;
    @PostMapping("/login/password")
    public R loginByPassword(@RequestBody LoginPasswordRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String account = null;
        String password= null;
        try {
            account = loginRequest.getAccount();
            password = loginRequest.getPassword();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        User user = null;
            if(FormatValidator.isEmail(account)){
                user = userMapper.selectUserWithRolesByEmail(account);
            }else if(FormatValidator.isPhone(account)){
                user = userMapper.selectUserWithRolesByPhone(account);
            }else {
               user =  userMapper.selectUserWithRolesByUsername(account);
            }
            if(user == null){
                throw new UsernameNotFoundException("登录账号："+account+"找到");
            }

        boolean isMatch = passwordEncoder.matches(password, user.getPassword());
         if(!isMatch){
             throw new BadCredentialsException("用户密码错误");
         }

        List<String> roles = user.getRoles().stream().map(Role::getCode).toList();
        UserInfo userInfo = UserInfo.builder()
                .uid(user.getUid())
                .username(user.getUsername())
                .roles(roles)
                .clientIp(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .build();

        String sessionId = sessionClient.creatSession(userInfo);
        Cookie cookie = new Cookie("SESSION_ID", sessionId);
        cookie.setHttpOnly(true);
        // 生产环境要换成 true
        cookie.setSecure(false);
        // 设置为 Lax 以便在重定向时携带 Cookie
        cookie.setAttribute("SameSite", "Lax");
        // 设置全局路径，确保授权服务能读取到
        cookie.setPath("/");
        // 先如此设置，之后在配置中心中配置获取
        cookie.setMaxAge(3600);
        response.addCookie(cookie);
        return R.success(null);
    }

    @PostMapping("/register")
    public R register(){
        return R.success(null);
    }
    @GetMapping("/logout")
    public R logout(){
        return R.success(null);
    }
    @GetMapping("/callback")
    public R authCallback(String code, String state){
        log.info("进行了回调 code+{},state:{}", code, state);
        return R.success(null);
    }

}
