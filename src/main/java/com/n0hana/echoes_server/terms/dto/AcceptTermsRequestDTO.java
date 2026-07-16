package com.n0hana.echoes_server.terms.dto;

import com.n0hana.echoes_server.terms.model.DocumentType;

public record AcceptTermsRequestDTO(
    String email,
    DocumentType type
) {}