package com.keli.clientserver.controller;

import com.keli.clientserver.dto.ClientAddRequest;
import com.keli.clientserver.dto.ClientAddResponse;
import com.keli.clientserver.dto.ClientDto;
import com.keli.clientserver.security.AuthContextResolver;
import com.keli.clientserver.security.AuthenticatedUser;
import com.keli.clientserver.service.OauthClientService;
import com.keli.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/addClient")
    public R<?> addClient(@RequestBody ClientAddRequest clientAddRequest, Authentication authentication) {
        try {
            AuthenticatedUser authenticatedUser = authContextResolver.resolve(authentication);
            clientAddRequest.setClientSecret(passwordEncoder.encode(clientAddRequest.getClientSecret()));
            ClientAddResponse clientAddResponse = clientService.addClient(clientAddRequest, authenticatedUser);
            return R.success(clientAddResponse);
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }
    }

    @GetMapping("/query")
    public R<?> queryClients(@RequestParam(value = "keyword", required = false) String keyword, Authentication authentication){
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
    public R<?> updateClient(@PathVariable String id, @RequestBody ClientAddRequest clientAddRequest, Authentication authentication) {
        try {
            AuthenticatedUser authenticatedUser = authContextResolver.resolve(authentication);
            if (clientAddRequest.getClientSecret() != null && !clientAddRequest.getClientSecret().isBlank()) {
                clientAddRequest.setClientSecret(passwordEncoder.encode(clientAddRequest.getClientSecret()));
            }
            ClientDto updated = clientService.updateClient(id, clientAddRequest, authenticatedUser);
            if (updated == null) {
                return R.error("客户端不存在");
            }
            return R.success(updated);
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }
    }

    @DeleteMapping("/deleteClient/{id}")
    public R<?> deleteClient(@PathVariable String id, Authentication authentication) {
        try {
            AuthenticatedUser authenticatedUser = authContextResolver.resolve(authentication);
            if (!clientService.deleteClient(id, authenticatedUser)) {
                return R.error("客户端不存在");
            }
            return R.success("删除成功", null);
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }
    }
}
