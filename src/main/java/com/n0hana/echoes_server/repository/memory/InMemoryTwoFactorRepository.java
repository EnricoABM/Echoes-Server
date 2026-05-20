package com.n0hana.echoes_server.repository.memory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.n0hana.echoes_server.dto.TwoFactorDto;

@Repository
public class InMemoryTwoFactorRepository implements InMemoryRepository<String, TwoFactorDto> {

    // Armazem
    private final Map<String, TwoFactorDto> storage = new ConcurrentHashMap<>();

    // Salvar os dados
    public TwoFactorDto save(TwoFactorDto token) {
        storage.put(token.email(), token);
        return token;
    }

    // Encontrar Requisição
    public Optional<TwoFactorDto> find(String email) {
        TwoFactorDto token = storage.get(email);

        if (token == null) {
            return Optional.empty();
        }

        // Logica de Auto Expiração
        if (token.expiresAt().isBefore(Instant.now())) {
            storage.remove(email);
            return Optional.empty();
        }

        return Optional.of(token);
    }

    // Deletar
    public void delete(String email) {
        storage.remove(email);
    }

    // Verificar se Existe
    public boolean exists(String email) {
        return storage.containsKey(email);
    }

    public String buildKey(String email) {
        return "";
    }

    @Override
    public TwoFactorDto save(String email, TwoFactorDto value, Duration tls) {
        return null;
    } 
}
