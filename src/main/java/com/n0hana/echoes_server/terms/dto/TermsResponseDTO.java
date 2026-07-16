package com.n0hana.echoes_server.terms.dto;

import java.time.Instant;

import com.n0hana.echoes_server.terms.model.DocumentType;

public record TermsResponseDTO(
    Long id,
    String version,
    String content,
    DocumentType type,
    boolean active,
    Instant timestamp
) {}
