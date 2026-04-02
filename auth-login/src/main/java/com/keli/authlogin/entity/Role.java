package com.keli.authlogin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String code;
    private String name;
    private String description;
}
