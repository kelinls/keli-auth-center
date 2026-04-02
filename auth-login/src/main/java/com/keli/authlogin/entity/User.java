package com.keli.authlogin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String uid;
    private String username;
    private String phone;
    private String email;
    private String password;
    private Integer status;      // 0禁用，1启用
    private Integer locked;      // 0未锁，1锁定
    private Integer loginFailCount;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 非表字段，用于关联查询
    @TableField(exist = false)
    private Set<Role> roles;
}