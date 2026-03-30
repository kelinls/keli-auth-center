package com.keli.authlogin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keli.authlogin.entity.LoginAuditLog;
import com.keli.authlogin.mapper.LoginAuditLogMapper;
import com.keli.authlogin.service.LoginAuditLogService;
import org.springframework.stereotype.Service;


/**
 * @author kelinls
* @description 针对表【login_audit_log(用户登录审计日志表)】的数据库操作Service实现
* @createDate 2026-03-22 14:30:37
*/
@Service
public class LoginAuditLogServiceImpl extends ServiceImpl<LoginAuditLogMapper, LoginAuditLog>
    implements LoginAuditLogService {

}




