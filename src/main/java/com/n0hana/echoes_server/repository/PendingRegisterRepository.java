package com.n0hana.echoes_server.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.n0hana.echoes_server.dto.RegisterRequestDTO;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Repository;

@Repository
public class PendingRegisterRepository {

    private final Cache<String, RegisterRequestDTO> cache =
        Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();

    public void save(RegisterRequestDTO dto) {
        cache.put(dto.email(), dto);
    }

    public RegisterRequestDTO find(String email) {
        return cache.getIfPresent(email);
    }

    public void delete(String email) {
        cache.invalidate(email);
    }
}
