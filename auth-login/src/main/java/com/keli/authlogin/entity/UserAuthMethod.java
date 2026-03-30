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
* @TableName user_auth_method
*/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserAuthMethod implements Serializable {

    /**
    * 
    */
    @NotNull(message="[]不能为空")
    @Schema(description = "")
    private Long id;
    /**
    * 
    */
    @NotBlank(message="[]不能为空")
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description = "")
    @Length(max= 64,message="编码长度不能超过64")
    private String uid;
    /**
    * password/phone/email/wechat/github...
    */
    @NotBlank(message="[password/phone/email/wechat/github...]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @Schema(description = "password/phone/email/wechat/github...")
    @Length(max= 32,message="编码长度不能超过32")
    private String identityType;
    /**
    * 用户名/手机号/邮箱/open_id
    */
    @NotBlank(message="[用户名/手机号/邮箱/open_id]不能为空")
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "用户名/手机号/邮箱/open_id")
    @Length(max= 255,message="编码长度不能超过255")
    private String identifier;
    /**
    * 密码哈希或第三方 access_token（备用）
    */
    @Size(max= 255,message="编码长度不能超过-1")
    @Schema(description = "密码哈希或第三方 access_token（备用）")
    @Length(max= 255,message="编码长度不能超过-1")
    private String credential;
    /**
    * 是否已验证
    */
    @Schema(description = "是否已验证")
    private Integer verified;
    /**
    * 第三方 access_token
    */
    @Size(max= 255,message="编码长度不能超过-1")
    @Schema(description = "第三方 access_token")
    @Length(max= 255,message="编码长度不能超过-1")
    private String accessToken;
    /**
    * 第三方 refresh_token
    */
    @Size(max= 255,message="编码长度不能超过-1")
    @Schema(description = "第三方 refresh_token")
    @Length(max= 255,message="编码长度不能超过-1")
    private String refreshToken;
    /**
    * access_token 过期时间
    */
    @Schema(description = "access_token 过期时间")
    private Date tokenExpiresAt;
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
