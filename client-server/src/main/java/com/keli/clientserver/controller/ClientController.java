package com.keli.clientserver.controller;

import com.keli.clientserver.dto.ClientAddRequest;
import com.keli.clientserver.dto.ClientAddResponse;
import com.keli.clientserver.dto.ClientDto;
import com.keli.clientserver.security.AuthContextResolver;
import com.keli.clientserver.security.AuthenticatedUser;
import com.keli.clientserver.service.OauthClientService;
import com.keli.common.audit.payload.OAuthClientMutationAuditPayload;
import com.keli.common.mq.AuthCenterAuditMqBridge;
import com.keli.common.mq.AuthCenterAuditMqConstants;
import com.keli.common.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    private OauthClientService clientService;
    @Autowired
    private AuthContextResolver authContextResolver;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final AuthCenterAuditMqBridge auditMqBridge;

    public ClientController(RocketMQTemplate rocketMQTemplate ) {
        this.auditMqBridge = new AuthCenterAuditMqBridge(rocketMQTemplate,true);
    }

    @PostMapping("/addClient")
    public R<?> addClient(
            @RequestBody ClientAddRequest clientAddRequest,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Date now = new Date();
        try {
            AuthenticatedUser authenticatedUser = authContextResolver.resolve(authentication);
            clientAddRequest.setClientSecret(passwordEncoder.encode(clientAddRequest.getClientSecret()));
            ClientAddResponse clientAddResponse = clientService.addClient(clientAddRequest, authenticatedUser);
            publishOauthClientAudit(
                    httpRequest,
                    authenticatedUser,
                    "ADD",
                    null,
                    clientAddResponse.getClientId(),
                    1,
                    null,
                    now);
            return R.success(clientAddResponse);
        } catch (Exception ex) {
            publishOauthClientAudit(
                    httpRequest,
                    resolveUserSafe(authentication),
                    "ADD",
                    null,
                    clientAddRequest != null ? clientAddRequest.getClientId() : null,
                    0,
                    ex.getMessage(),
                    now);
            return R.error(ex.getMessage());
        }
    }

    @GetMapping("/query")
    public R<?> queryClients(@RequestParam(value = "keyword", required = false) String keyword, Authentication authentication) {
        try {
            AuthenticatedUser authenticatedUser = authContextResolver.resolve(authentication);
            List<ClientDto> clients = clientService.listClients(keyword, authenticatedUser);
            return R.success(clients);
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }
    }

    @GetMapping("/getClient/{clientId}")
    public R<?> getClient(@PathVariable String clientId, Authentication authentication) {
        try {
            AuthenticatedUser authenticatedUser = authContextResolver.resolve(authentication);
            ClientDto client = clientService.getClientByClientId(clientId, authenticatedUser);
            return R.success(client);
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }
    }

    @PutMapping("/updateClient/{id}")
    public R<?> updateClient(
            @PathVariable String id,
            @RequestBody ClientAddRequest clientAddRequest,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Date now = new Date();
        try {
            AuthenticatedUser authenticatedUser = authContextResolver.resolve(authentication);
            if (clientAddRequest.getClientSecret() != null && !clientAddRequest.getClientSecret().isBlank()) {
                clientAddRequest.setClientSecret(passwordEncoder.encode(clientAddRequest.getClientSecret()));
            }
            ClientDto updated = clientService.updateClient(id, clientAddRequest, authenticatedUser);
            if (updated == null) {
                publishOauthClientAudit(
                        httpRequest,
                        authenticatedUser,
                        "UPDATE",
                        id,
                        clientAddRequest != null ? clientAddRequest.getClientId() : null,
                        0,
                        "客户端不存在",
                        now);
                return R.error("客户端不存在");
            }
            publishOauthClientAudit(
                    httpRequest,
                    authenticatedUser,
                    "UPDATE",
                    id,
                    updated.getClientId(),
                    1,
                    null,
                    now);
            return R.success(updated);
        } catch (Exception ex) {
            publishOauthClientAudit(
                    httpRequest,
                    resolveUserSafe(authentication),
                    "UPDATE",
                    id,
                    clientAddRequest != null ? clientAddRequest.getClientId() : null,
                    0,
                    ex.getMessage(),
                    now);
            return R.error(ex.getMessage());
        }
    }

    @DeleteMapping("/deleteClient/{id}")
    public R<?> deleteClient(
            @PathVariable String id, Authentication authentication, HttpServletRequest httpRequest) {
        Date now = new Date();
        try {
            AuthenticatedUser authenticatedUser = authContextResolver.resolve(authentication);
            if (!clientService.deleteClient(id, authenticatedUser)) {
                publishOauthClientAudit(
                        httpRequest,
                        authenticatedUser,
                        "DELETE",
                        id,
                        null,
                        0,
                        "客户端不存在",
                        now);
                return R.error("客户端不存在");
            }
            publishOauthClientAudit(
                    httpRequest,
                    authenticatedUser,
                    "DELETE",
                    id,
                    null,
                    1,
                    null,
                    now);
            return R.success("删除成功", null);
        } catch (Exception ex) {
            publishOauthClientAudit(
                    httpRequest,
                    resolveUserSafe(authentication),
                    "DELETE",
                    id,
                    null,
                    0,
                    ex.getMessage(),
                    now);
            return R.error(ex.getMessage());
        }
    }

    private AuthenticatedUser resolveUserSafe(Authentication authentication) {
        try {
            return authContextResolver.resolve(authentication);
        } catch (Exception e) {
            return new AuthenticatedUser("unknown", "unknown", java.util.Set.of());
        }
    }

    private void publishOauthClientAudit(
            HttpServletRequest httpRequest,
            AuthenticatedUser user,
            String operation,
            String oauthClientInternalId,
            String clientId,
            int status,
            String failureReason,
            Date occurredAt) {
        if (auditMqBridge == null) {
            return;
        }
        OAuthClientMutationAuditPayload payload =
                OAuthClientMutationAuditPayload.builder()
                        .operation(operation)
                        .oauthClientInternalId(oauthClientInternalId)
                        .clientId(clientId)
                        .operatorUserId(user.userId())
                        .operatorUsername(user.username())
                        .ipAddress(clientIp(httpRequest))
                        .status(status)
                        .failureReason(failureReason)
                        .occurredAt(occurredAt)
                        .build();
        auditMqBridge.publish(AuthCenterAuditMqConstants.TAG_OAUTH_CLIENT, payload);
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
