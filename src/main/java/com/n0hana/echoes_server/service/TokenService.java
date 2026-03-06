package com.n0hana.echoes_server.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.n0hana.echoes_server.model.Token;
import com.n0hana.echoes_server.model.TokenType;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.repository.TokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

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

            this.revokeAllUserTokens(user);
            this.saveToken(token, user);

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

    public Token saveToken(String jwtToken, User user) {
        Token token = Token.builder()
            .token(jwtToken)
            .type(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .user(user)
            .build();
        
        return tokenRepository.save(token);
    }

    public void revokeAllUserTokens(User user) {
        List<Token> list = tokenRepository.findAllByUserId(user.getId());

        list.forEach((token) -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(list);
    } 
}
