package com.n0hana.echoes_server.dto;

import com.n0hana.echoes_server.model.DocumentType;

public record TermsResponseDTO(
    Long id,
    String version,
    String content,
    DocumentType type,
    boolean active
) {}