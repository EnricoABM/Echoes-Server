package com.n0hana.echoes_server.password;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InMemoryPasswordCodeRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String PREFIX = "passwordcode:";

    @Data
    public static class PasswordCode
    { 
        private String email;
        private String code;
        private Instant expiredAt;
        private CodeType type;
    }

    public enum CodeType {
        CHANGE("change"),
        RESET("reset"),
        REACTIVATE("reactivate");

        private String type;

        private CodeType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public void save(PasswordCode code) {
      redisTemplate.opsForValue()
        .set(PREFIX + code.getEmail(), code, 5, TimeUnit.MINUTES);
    }

    public void delete(String email) {
      String key = PREFIX + email;
      redisTemplate.opsForValue().getAndDelete(key);
    }

    public PasswordCode getCode(String email) {
      String key = PREFIX + email;
      Object cache = redisTemplate.opsForValue().get(key);
      return (PasswordCode) cache;
    }

}
