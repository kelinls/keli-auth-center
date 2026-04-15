package com.keli.authlogin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keli.authlogin.common.emun.UserStatus;
import com.keli.authlogin.common.utils.FormatValidator;
import com.keli.authlogin.dto.RegisterRequest;
import com.keli.authlogin.dto.UserInfoUserCreateRequest;
import com.keli.authlogin.entity.User;
import com.keli.authlogin.feign.UserInfoClient;
import com.keli.authlogin.mapper.UserMapper;
import com.keli.authlogin.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * @author kelinls
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2026-03-22 14:30:37
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserInfoClient userInfoClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest registerRequest) {
        validateRegisterRequest(registerRequest);
        checkDuplicate(registerRequest);

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        String uid = UUID.randomUUID().toString();

        UserInfoUserCreateRequest createRequest = new UserInfoUserCreateRequest();
        createRequest.setUid(uid);
        createRequest.setUsername(registerRequest.getUsername().trim());
        createRequest.setPassword(encodedPassword);
        createRequest.setEmail(blankToNull(registerRequest.getEmail()));
        createRequest.setPhone(blankToNull(registerRequest.getPhone()));
        createRequest.setStatus(UserStatus.ACTIVE.getCode());
        createRequest.setNickname(blankToNull(registerRequest.getNickname()));
        createRequest.setGender(parseGender(registerRequest.getGender()));
        createRequest.setBirthday(parseBirthday(registerRequest.getBirthday()) == null ? null : registerRequest.getBirthday().trim());
        userInfoClient.createUser(createRequest);

    }

    private void validateRegisterRequest(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new IllegalArgumentException("注册参数不能为空");
        }
        if (isBlank(registerRequest.getUsername()) || registerRequest.getUsername().trim().length() < 3) {
            throw new IllegalArgumentException("用户名长度不能小于3");
        }
        if (isBlank(registerRequest.getPassword()) || registerRequest.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能小于6");
        }
        if (!isBlank(registerRequest.getEmail()) && !FormatValidator.isEmail(registerRequest.getEmail().trim())) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (!isBlank(registerRequest.getPhone()) && !FormatValidator.isPhone(registerRequest.getPhone().trim())) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (isBlank(registerRequest.getEmail()) && isBlank(registerRequest.getPhone())) {
            throw new IllegalArgumentException("手机号和邮箱不能同时为空");
        }
        parseBirthday(registerRequest.getBirthday());
        parseGender(registerRequest.getGender());
    }

    private void checkDuplicate(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername().trim();
        boolean usernameExists = userInfoClient.queryByUsername(username) != null;
        if (usernameExists) {
            throw new IllegalArgumentException("用户名已存在");
        }

        if (!isBlank(registerRequest.getEmail())) {
            String email = registerRequest.getEmail().trim();
            boolean emailExists = userInfoClient.queryByEmail(email) != null;
            if (emailExists) {
                throw new IllegalArgumentException("邮箱已被注册");
            }
        }

        if (!isBlank(registerRequest.getPhone())) {
            String phone = registerRequest.getPhone().trim();
            boolean phoneExists = userInfoClient.queryByPhone(phone) != null;
            if (phoneExists) {
                throw new IllegalArgumentException("手机号已被注册");
            }
        }
    }



    private LocalDate parseBirthday(String birthday) {
        if (isBlank(birthday)) {
            return null;
        }
        try {
            return LocalDate.parse(birthday.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("生日格式错误，期望 yyyy-MM-dd");
        }
    }

    private Integer parseGender(String gender) {
        if (isBlank(gender)) {
            return 0;
        }
        String normalized = gender.trim().toLowerCase();
        return switch (normalized) {
            case "1", "male", "man", "m", "男" -> 1;
            case "2", "female", "woman", "f", "女" -> 2;
            case "0", "unknown", "u", "未知" -> 0;
            default -> throw new IllegalArgumentException("性别值不合法");
        };
    }

    private String blankToNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

