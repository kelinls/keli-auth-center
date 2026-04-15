package com.keli.clientserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientAddResponse {
    private String clientId;
    private String clientName;
    private List<String> scopes;
}
