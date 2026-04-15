package com.keli.clientserver.controller;

import com.keli.clientserver.service.OauthClientService;
import com.keli.common.dto.RegisteredClientDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal")
public class internalController {
    @Autowired
    private OauthClientService oauthClientService;
    @GetMapping("/queryClients")
    public List<RegisteredClientDTO> queryClients(){
        return oauthClientService.listRegisteredClients();
    }

    @PostMapping("/getClientByClientId")
    public RegisteredClientDTO getClientByClientId(@RequestParam String clientId) {
        return oauthClientService.getRegisteredClient(clientId);
    }
    @PostMapping("/getClientById")
    public RegisteredClientDTO getClientById(@RequestParam String id) {
        return oauthClientService.getRegisteredClient(id);
    }

}
