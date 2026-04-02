package com.keli.authlogin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keli.authlogin.entity.User;
import org.apache.ibatis.annotations.Mapper;


/**
* @author kelinls
* @description 针对表【user】的数据库操作Mapper
* @createDate 2026-03-22 14:30:37
* @Entity com.keli.authserver.domain.User
*/

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User selectUserWithRolesByUsername(String username);
    User selectUserWithRolesByPhone(String phoneNumber);
    User selectUserWithRolesByEmail(String email);
}




