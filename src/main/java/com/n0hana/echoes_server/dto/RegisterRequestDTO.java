package com.n0hana.echoes_server.dto;

public record RegisterRequestDTO(
    String name,
    String email,
    String password
) {
    
}
