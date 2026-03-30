package com.keli.authlogin.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

/**
* 
* @TableName user
*/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User implements Serializable {

    /**
    * 主键
    */
    @NotNull(message="[主键]不能为空")
    @Schema(description = "主键")
    private Long id;
    /**
    * 用户唯一标识uid
    */
    @NotBlank(message="[用户唯一标识uid]不能为空")
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description = "用户唯一标识uid")
    @Length(max= 64,message="编码长度不能超过64")
    private String uid;
    /**
    * 昵称
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "昵称")
    @Length(max= 255,message="编码长度不能超过255")
    private String nickname;
    /**
    * 头像 URL
    */
    @Size(max= -1,message="编码长度不能超过-1")
    @Schema(description = "头像 URL")
    @Length(max= -1,message="编码长度不能超过-1")
    private String avatar;
    /**
    * 0-正常 1-冻结 2-注销
    */
    @Schema(description = "0-正常 1-冻结 2-注销")
    private Integer status;
    /**
    * 
    */
    @Schema(description = "")
    private Date createdAt;
    /**
    * 
    */
    @Schema(description = "")
    private Date updatedAt;

}
