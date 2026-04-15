package com.keli.authserver.feign;

import com.keli.common.dto.RegisteredClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "client-server")
public interface ClientManagementFeignClient {
    @PostMapping("/internal/getClientByClientId")
    RegisteredClientDTO getClientByClientId(@RequestParam String clientId);
    @PostMapping("/internal/getClientById")
    RegisteredClientDTO getClientById(@RequestParam String id);

    @GetMapping("/internal/queryClients")
    List<RegisteredClientDTO> getAllClients();
}
