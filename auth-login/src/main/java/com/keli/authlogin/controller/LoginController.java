package com.keli.authlogin.controller;


import com.keli.authlogin.common.exception.loginException.AccountDisabledException;
import com.keli.authlogin.common.exception.loginException.BadCredentialsException;
import com.keli.authlogin.common.exception.loginException.TooManyAttemptsException;
import com.keli.authlogin.common.exception.loginException.UsernameNotFoundException;
import com.keli.authlogin.common.utils.FormatValidator;
import com.keli.authlogin.dto.LoginPasswordRequest;
import com.keli.authlogin.dto.LoginValidInfo;
import com.keli.authlogin.dto.RegisterRequest;
import com.keli.authlogin.dto.UserInfoUserDto;
import com.keli.authlogin.entity.Role;
import com.keli.authlogin.entity.User;
import com.keli.authlogin.feign.SessionClient;
import com.keli.authlogin.feign.UserInfoClient;
import com.keli.authlogin.service.LoginFailCounter;
import com.keli.authlogin.service.UserService;
import com.keli.authlogin.service.impl.UserSecurityValidator;
import com.keli.common.dto.UserInfo;
import com.keli.common.utils.R;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class LoginController {
    @Autowired
    private UserInfoClient userInfoClient;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SessionClient sessionClient;
    @Autowired
    private LoginFailCounter loginFailCounter;
    @Autowired
    private UserService userService;
    @PostMapping("/login/password")
    public R loginByPassword(@RequestBody LoginPasswordRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String account = null;
        String unverifiedPassword= null;
        User user = null;
        try {
            account = loginRequest.getAccount();
            unverifiedPassword = loginRequest.getPassword();
            if(FormatValidator.isEmail(account)){
                user = toLoginUser(userInfoClient.queryByEmail(account));
            }else if(FormatValidator.isPhone(account)){
                user = toLoginUser(userInfoClient.queryByPhone(account));
            }else {
               user = toLoginUser(userInfoClient.queryByUsername(account));
            }
            if(user == null){
                throw new UsernameNotFoundException("登录账号："+account+"找到");
            }

            LoginValidInfo loginValidInfo = new LoginValidInfo();
            loginValidInfo.setUnverifiedPassword(unverifiedPassword);
            loginValidInfo.setUser(user);
            UserSecurityValidator validator = new UserSecurityValidator(loginValidInfo,passwordEncoder,loginFailCounter);
            validator.validate();
        }catch (UsernameNotFoundException e){
            log.error(e.getMessage());
            return R.error("未找到账号");
        } catch (BadCredentialsException e) {
            log.error(e.getMessage());
            assert user != null;
            loginFailCounter.countLoginFail(user.getUid(),request.getRemoteAddr());
           return R.error("用户密码错误");
        }catch (AccountDisabledException e){
            log.error(e.getMessage());
            return R.error("用户已被封禁");
        }catch (TooManyAttemptsException e){
            return R.error("尝试太多次");
        }
        computeSession(user,request,response);
        return R.success("登录成功");
    }
    private void computeSession(User user,HttpServletRequest request,HttpServletResponse response){
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
    }

    @PostMapping("/register")
    public R register(@RequestBody RegisterRequest registerRequest) {
        try {
            userService.register(registerRequest);
            return R.success("注册成功");
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("注册失败", e);
            return R.error("注册失败");
        }
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

    private User toLoginUser(UserInfoUserDto userDto) {
        if (userDto == null) {
            return null;
        }
        User user = new User();
        user.setId(userDto.getId());
        user.setUid(userDto.getUid());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setPassword(userDto.getPassword());
        user.setStatus(userDto.getStatus());
        List<String> roleCodes = userDto.getRoles() == null ? Collections.emptyList() : userDto.getRoles();
        user.setRoles(new HashSet<>(roleCodes.stream().map(code -> new Role(null, code, code, null)).toList()));
        return user;
    }

}
