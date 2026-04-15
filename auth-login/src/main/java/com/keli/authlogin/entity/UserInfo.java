package com.keli.authlogin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_info")
public class UserInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String uid;            // 关联 user.uid
    private String nickname;       // 昵称
    private Integer gender;        // 0-未知，1-男，2-女
    private LocalDate birthday;    // 出生日期
    private String avatar;         // 头像URL
    private String bio;            // 个人简介

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
