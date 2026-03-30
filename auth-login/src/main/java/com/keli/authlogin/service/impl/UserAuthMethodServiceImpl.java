package com.keli.authlogin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.keli.authlogin.entity.UserAuthMethod;
import com.keli.authlogin.mapper.UserAuthMethodMapper;
import com.keli.authlogin.service.UserAuthMethodService;
import org.springframework.stereotype.Service;

/**
 * @author kelinls
* @description 针对表【user_auth_method】的数据库操作Service实现
* @createDate 2026-03-22 14:30:37
*/
@Service
public class UserAuthMethodServiceImpl extends ServiceImpl<UserAuthMethodMapper, UserAuthMethod>
    implements UserAuthMethodService {

}




