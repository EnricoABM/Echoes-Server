package com.n0hana.echoes_server.dto;

import java.time.Instant;

public record TwoFactorDto(
    String code,
    Instant expiresAt,
    RegisterRequestDTO dto
    ) {}
