package com.keli.userinfoserver.controller;

import com.keli.common.audit.payload.UserInfoMutationAuditPayload;
import com.keli.common.mq.AuthCenterAuditMqBridge;
import com.keli.common.mq.AuthCenterAuditMqConstants;
import com.keli.common.utils.R;
import com.keli.userinfoserver.audit.JwtOperatorResolver;
import com.keli.userinfoserver.dto.UserDetailResponse;
import com.keli.userinfoserver.dto.UserInfoUpsertRequest;
import com.keli.userinfoserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {

    @Autowired
    private  UserService userService;

    private final AuthCenterAuditMqBridge auditMqBridge;

    public UserInfoController(RocketMQTemplate rocketMQTemplate) {
        this.auditMqBridge = new  AuthCenterAuditMqBridge(rocketMQTemplate,true);
    }

    @Autowired
    private JwtOperatorResolver jwtOperatorResolver;



    @GetMapping("/user/{uid}")
    public R<UserDetailResponse> getByUid(@PathVariable String uid) {
        UserDetailResponse result = userService.getByUid(uid);
        if (result == null) {
            return R.error("用户不存在");
        }
        return R.success(result);
    }

    @PostMapping("/user")
    public R<UserDetailResponse> create(@RequestBody UserInfoUpsertRequest request, HttpServletRequest httpRequest) {
        Date now = new Date();
        UserDetailResponse created = userService.create(request);
        publishUserInfoAudit(httpRequest, "CREATE", created.getUid(), 1, null, now);
        return R.success(created);
    }

    @PutMapping("/user/{uid}")
    public R<UserDetailResponse> update(
            @PathVariable String uid, @RequestBody UserInfoUpsertRequest request, HttpServletRequest httpRequest) {
        Date now = new Date();
        UserDetailResponse updated = userService.update(uid, request);
        if (updated == null) {
            publishUserInfoAudit(httpRequest, "UPDATE", uid, 0, "用户不存在", now);
            return R.error("用户不存在");
        }
        publishUserInfoAudit(httpRequest, "UPDATE", uid, 1, null, now);
        return R.success(updated);
    }

    @DeleteMapping("/user/{uid}")
    public R<Void> delete(@PathVariable String uid, HttpServletRequest httpRequest) {
        Date now = new Date();
        if (!userService.delete(uid)) {
            publishUserInfoAudit(httpRequest, "DELETE", uid, 0, "用户不存在", now);
            return R.error("用户不存在");
        }
        publishUserInfoAudit(httpRequest, "DELETE", uid, 1, null, now);
        return R.success("删除成功", null);
    }

    private void publishUserInfoAudit(
            HttpServletRequest request,
            String operation,
            String targetUid,
            int status,
            String failureReason,
            Date occurredAt) {
        if (auditMqBridge == null) {
            return;
        }
        UserInfoMutationAuditPayload payload =
                UserInfoMutationAuditPayload.builder()
                        .operation(operation)
                        .targetUid(targetUid)
                        .operatorUid(jwtOperatorResolver.currentSubject().orElse(null))
                        .ipAddress(clientIp(request))
                        .userAgent(request.getHeader("User-Agent"))
                        .status(status)
                        .failureReason(failureReason)
                        .occurredAt(occurredAt)
                        .build();
        auditMqBridge.publish(AuthCenterAuditMqConstants.TAG_USER_INFO, payload);
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
