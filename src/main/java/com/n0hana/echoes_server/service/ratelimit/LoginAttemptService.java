package com.n0hana.echoes_server.service.ratelimit;

import java.time.Duration;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    
    private final int MAX_ATTEMPTS = 5;
    private final Duration LOCK_TIME = Duration.ofMinutes(30);

    public void loginFailed(String email) {

    }

    public void loginSucceeded(String email) {

    }
}
