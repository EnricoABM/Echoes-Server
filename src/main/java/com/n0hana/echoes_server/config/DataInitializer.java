package com.n0hana.echoes_server.config;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.n0hana.echoes_server.model.DocumentType;
import com.n0hana.echoes_server.model.Terms;
import com.n0hana.echoes_server.repository.TermsRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final TermsRepository termsRepository;

    @Bean
    public CommandLineRunner initTerms() {
        return args -> {
            Arrays.stream(DocumentType.values()).forEach(type -> {
                if (termsRepository.findTopByTypeOrderByTimestampDesc(type).isEmpty()) {
                    Terms terms = new Terms();
                    terms.setVersion("1.0.0");
                    terms.setContent(getDefaultContent(type));
                    terms.setType(type);
                    terms.setActive(true);
                    termsRepository.save(terms);
                }
            });
        };
    }

    private String getDefaultContent(DocumentType type) {
        return switch (type) {
            case TERMS_OF_USE -> "Terms of Use v1.0.0 - Default content.";
            case PRIVACY_POLICY -> "Privacy Policy v1.0.0 - Default content.";
            case DATA_DELETION_POLICY -> "Data Deletion Policy v1.0.0 - Default content.";
            case MARKETING_CONSENT -> "Marketing Consent v1.0.0 - Default content.";
            case COOKIES_POLICY -> "Cookies Policy v1.0.0 - Default content.";
        };
    }
}