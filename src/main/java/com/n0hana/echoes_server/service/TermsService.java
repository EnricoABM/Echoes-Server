package com.n0hana.echoes_server.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.n0hana.echoes_server.model.DocumentType;
import com.n0hana.echoes_server.model.Terms;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.model.UserTermsAcceptance;
import com.n0hana.echoes_server.repository.TermsRepository;
import com.n0hana.echoes_server.repository.UserRepository;
import com.n0hana.echoes_server.repository.UserTermsAcceptanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;
    private final UserTermsAcceptanceRepository userTermsAcceptanceRepository;
    private final UserRepository userRepository;

    public Terms getActiveTerms(DocumentType type) {
        return termsRepository.findByTypeAndActiveTrue(type)
            .orElseThrow(() -> new RuntimeException("No active terms found for type: " + type));
    }

    public Terms getLatestTerms(DocumentType type) {
        return termsRepository.findTopByTypeOrderByTimestampDesc(type)
            .orElseThrow(() -> new RuntimeException("No terms found for type: " + type));
    }

    public boolean hasAcceptedLatestTerms(UUID userId, DocumentType type) {
        return userTermsAcceptanceRepository.findLatestByUserIdAndType(userId, type)
            .map(acceptance -> {
                Terms latestTerms = getLatestTerms(type);
                return acceptance.getTerms().getId() == latestTerms.getId();
            })
            .orElse(false);
    }

    public UserTermsAcceptance acceptTerms(UUID userId, DocumentType type) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Terms terms = getActiveTerms(type);
        
        UserTermsAcceptance acceptance = new UserTermsAcceptance(user, terms);
        return userTermsAcceptanceRepository.save(acceptance);
    }

    public List<UserTermsAcceptance> getUserAcceptances(UUID userId) {
        return userTermsAcceptanceRepository.findAll().stream()
            .filter(a -> a.getUser().getId().equals(userId))
            .toList();
    }
}
