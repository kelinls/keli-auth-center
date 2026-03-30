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
* 用户登录审计日志表
* @TableName login_audit_log
*/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginAuditLog implements Serializable {

    /**
    * 记录唯一ID
    */
    @NotNull(message="[记录唯一ID]不能为空")
    @Schema(description = "记录唯一ID")
    private Long id;
    /**
    * 用户ID（登录成功时必填，失败时可为空）
    */
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description = "用户ID（登录成功时必填，失败时可为空）")
    @Length(max= 64,message="编码长度不能超过64")
    private String uid;
    /**
    * 登录发生时间
    */
    @NotNull(message="[登录发生时间]不能为空")
    @Schema(description = "登录发生时间")
    private Date loginTime;
    /**
    * 登录IP（支持IPv4和IPv6）
    */
    @NotBlank(message="[登录IP（支持IPv4和IPv6）]不能为空")
    @Size(max= 45,message="编码长度不能超过45")
    @Schema(description = "登录IP（支持IPv4和IPv6）")
    @Length(max= 45,message="编码长度不能超过45")
    private String ipAddress;
    /**
    * 登录方式：password/sms/wechat/github/...
    */
    @NotBlank(message="[登录方式：password/sms/wechat/github/...]不能为空")
    @Size(max= 32,message="编码长度不能超过32")
    @Schema(description = "登录方式：password/sms/wechat/github/...")
    @Length(max= 32,message="编码长度不能超过32")
    private String loginType;
    /**
    * 登录结果：1-成功，0-失败
    */
    @NotNull(message="[登录结果：1-成功，0-失败]不能为空")
    @Schema(description = "登录结果：1-成功，0-失败")
    private Integer status;
    /**
    * 失败原因（如：密码错误、账号锁定、验证码错误等）
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "失败原因（如：密码错误、账号锁定、验证码错误等）")
    @Length(max= 255,message="编码长度不能超过255")
    private String failureReason;
    /**
    * 用户代理字符串（浏览器/设备信息）
    */
    @Size(max= -1,message="编码长度不能超过-1")
    @Schema(description = "用户代理字符串（浏览器/设备信息）")
    @Length(max= -1,message="编码长度不能超过-1")
    private String userAgent;
    /**
    * 设备指纹（可选，用于识别可信设备）
    */
    @Size(max= 128,message="编码长度不能超过128")
    @Schema(description = "设备指纹（可选，用于识别可信设备）")
    @Length(max= 128,message="编码长度不能超过128")
    private String deviceId;
    /**
    * 地理位置（根据IP解析，如：中国-北京）
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "地理位置（根据IP解析，如：中国-北京）")
    @Length(max= 255,message="编码长度不能超过255")
    private String location;
    /**
    * OAuth2客户端ID（如果是第三方应用授权登录）
    */
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description = "OAuth2客户端ID（如果是第三方应用授权登录）")
    @Length(max= 64,message="编码长度不能超过64")
    private String clientId;
    /**
    * 本次登录生成的会话ID或token标识（用于关联后续操作）
    */
    @Size(max= 128,message="编码长度不能超过128")
    @Schema(description = "本次登录生成的会话ID或token标识（用于关联后续操作）")
    @Length(max= 128,message="编码长度不能超过128")
    private String sessionId;
    /**
    * 是否使用了多因素认证
    */
    @Schema(description = "是否使用了多因素认证")
    private Integer mfaUsed;
    /**
    * 登录时尝试的用户名/手机号/邮箱（失败时记录，便于分析）
    */
    @Size(max= 255,message="编码长度不能超过255")
    @Schema(description = "登录时尝试的用户名/手机号/邮箱（失败时记录，便于分析）")
    @Length(max= 255,message="编码长度不能超过255")
    private String identifierAttempted;
    /**
    * 记录创建时间（通常与login_time一致）
    */
    @NotNull(message="[记录创建时间（通常与login_time一致）]不能为空")
    @Schema(description = "记录创建时间（通常与login_time一致）")
    private Date createdAt;



}
