package com.n0hana.echoes_server.dto;

import com.n0hana.echoes_server.model.DocumentType;

public record ReactivateRequestDTO(
    String email,
    String code,
    DocumentType type
) {}