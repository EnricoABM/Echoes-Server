package com.n0hana.echoes_server.service.auth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.n0hana.echoes_server.model.Token;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.repository.JwtTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtTokenRepository jwtTokenRepository;

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
            .withIssuer("auth-api")
            .withSubject(user.getEmail())
            .withExpiresAt(genExpirationDate())
            .sign(algorithm);

            this.save(token, user);

            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao criar token JWT");
        }
    }

    public String validadeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                .withIssuer("auth-api")
                .build()
                .verify(token)
                .getSubject();    
        } catch (JWTVerificationException e) {
            System.err.println("Erro na verificação: " + e.getMessage());
            return "";
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusHours(10).toInstant(ZoneOffset.of("-03:00"));
    }

    public void save(String jwt, User user) {
        this.revokeAll(user);

        Token token = new Token(
            null,
            jwt,
            false,
            user
        );

        jwtTokenRepository.save(token);;
    }

    public void revokeAll(User user) {
        List<Token> tokens = jwtTokenRepository.findAllByUserId(user.getId()); 

        tokens.forEach(t -> t.setRevoked(true));

        jwtTokenRepository.saveAll(tokens);
    }

    public Optional<Token> findByToken(String token) {
        return jwtTokenRepository.findByToken(token);
    }
}
