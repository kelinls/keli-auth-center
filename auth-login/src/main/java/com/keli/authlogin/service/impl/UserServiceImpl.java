package com.keli.authlogin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.keli.authlogin.entity.User;
import com.keli.authlogin.mapper.UserMapper;
import com.keli.authlogin.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author kelinls
* @description 针对表【user】的数据库操作Service实现
* @createDate 2026-03-22 14:30:37
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




