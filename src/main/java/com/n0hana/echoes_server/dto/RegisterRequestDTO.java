package com.n0hana.echoes_server.dto;

import com.n0hana.echoes_server.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterRequestDTO(
    @NotBlank
    String name,

    @NotBlank
    @Email(message="E-mail inválido")
    String email,

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
        message = "Senha fraca"
    )
    String password,

    @NotNull
    UserRole role
) {
    
}
