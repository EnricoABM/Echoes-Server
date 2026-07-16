package com.n0hana.echoes_server.user;

public record UserInfoResponseDTO(
    String name,
    String email,
    String role
) {
    
}
