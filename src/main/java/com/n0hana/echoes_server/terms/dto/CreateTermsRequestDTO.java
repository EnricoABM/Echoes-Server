package com.n0hana.echoes_server.terms.dto;

import com.n0hana.echoes_server.terms.model.DocumentType;

public record CreateTermsRequestDTO(
    String version,
    String content,
    DocumentType type
) {}
