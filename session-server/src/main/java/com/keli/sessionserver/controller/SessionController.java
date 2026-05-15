package com.keli.sessionserver.controller;

import com.keli.common.audit.payload.SessionLifecycleAuditPayload;
import com.keli.common.dto.SsoSessionPrincipal;
import com.keli.common.dto.SsoTokenCredentials;
import com.keli.common.dto.UserInfo;
import com.keli.common.mq.AuthCenterAuditMqBridge;
import com.keli.common.mq.AuthCenterAuditMqConstants;
import com.keli.common.utils.R;
import com.keli.sessionserver.common.utils.SessionIdGenerator;
import com.keli.sessionserver.dto.SessionData;
import com.keli.sessionserver.service.SessionSecurityValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/session")
public class SessionController {
    private final String sessionKeyPrefix = "session:";
    @Autowired
    private RedisTemplate<String,SessionData> redisTemplate;

    @Autowired
    private SessionSecurityValidator sessionSecurityValidator;




    @Value("${security.session.expireTtl}")
    private Integer SessionExpireTtl;

    private final AuthCenterAuditMqBridge auditMqBridge;

    public SessionController(RocketMQTemplate rocketMQTemplate) {
        this.auditMqBridge = new  AuthCenterAuditMqBridge(rocketMQTemplate,true);
    }

    @PostMapping("/validate")
    public SsoSessionPrincipal validateSession(@RequestBody SsoTokenCredentials credentials) {
//        if(credentials != null){
//            return null;
//        }
        String sessionKey = sessionKeyPrefix + credentials.getSessionId();
        SessionData sessionData = redisTemplate.opsForValue().get(sessionKey);
        //未找到session
        if(sessionData == null) return null;
        //已经过期
        if(sessionData.getExpiredAt() < System.currentTimeMillis()){
            log.info("sessionId:{} expired at: {},userId:{},username:{}", credentials.getSessionId(),sessionData.getExpiredAt(),sessionData.getUid(),sessionData.getUsername());
            return null;
        }

        boolean validate = sessionSecurityValidator.validate(sessionData, credentials);

        if(!validate) {
            return null;
        }
        SsoSessionPrincipal principal = new SsoSessionPrincipal();
        principal.setSessionId(credentials.getSessionId());
        principal.setUid(sessionData.getUid());
        principal.setUsername(sessionData.getUsername());
        principal.setRoles(sessionData.getRoles());
        //滑动更新session持续时间
        sessionData.setExpiredAt(System.currentTimeMillis()+SessionExpireTtl*1000);
        redisTemplate.expire(sessionKey, Duration.ofSeconds(SessionExpireTtl));
        return principal;
    }



    @PostMapping("/createSession")
    public String creatSession(@RequestBody UserInfo userInfo){
        long now = System.currentTimeMillis();
        String sessionId = SessionIdGenerator.generateSessionId();
        SessionData sessionData = SessionData.builder()
                .uid(userInfo.getUid())
                .username(userInfo.getUsername())
                .roles(userInfo.getRoles())
                .createdAt(now)
                .expiredAt(now + SessionExpireTtl*1000)
                .lastAccessAt(now)
                .clientIp(userInfo.getClientIp())
                .userAgent(userInfo.getUserAgent())
                .build();
        String sessionKey = computeSessionKey(sessionId);

        try {
            redisTemplate.opsForValue().set(sessionKey, sessionData);
            redisTemplate.expire(sessionKey, Duration.ofSeconds(SessionExpireTtl));
        } catch (Exception e) {
            log.error("往redis存储session出错");
            throw new RuntimeException(e);
        }
        logSessionGenerated(sessionData);
        if (auditMqBridge != null) {
            auditMqBridge.publish(
                    AuthCenterAuditMqConstants.TAG_SESSION,
                    SessionLifecycleAuditPayload.builder()
                            .operation("CREATE")
                            .sessionId(sessionId)
                            .uid(userInfo.getUid())
                            .username(userInfo.getUsername())
                            .clientIp(userInfo.getClientIp())
                            .userAgent(userInfo.getUserAgent())
                            .success(1)
                            .occurredAt(new Date())
                            .build());
        }
        return  sessionId;
    }
    @PostMapping("test")
    public R test(){
        return R.success(null);
    }

    private void logSessionGenerated(SessionData sessionData) {
    }

    String computeSessionKey(String sessionId){
        return sessionKeyPrefix + sessionId;
    }

}
