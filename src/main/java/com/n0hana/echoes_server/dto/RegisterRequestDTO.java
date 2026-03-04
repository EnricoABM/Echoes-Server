package com.n0hana.echoes_server.dto;

import com.n0hana.echoes_server.model.UserRole;

public record RegisterRequestDTO(
    String name,
    String email,
    String password,
    UserRole role
) {
    
}
