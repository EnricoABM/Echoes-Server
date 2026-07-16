package com.n0hana.echoes_server.terms.dto;

public record ReactivateRequestDTO(
    String email,
    String code
) {}