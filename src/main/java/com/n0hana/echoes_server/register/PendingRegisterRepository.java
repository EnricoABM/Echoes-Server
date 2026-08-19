package com.n0hana.echoes_server.register;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PendingRegisterRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String PREFIX = "register:";

    public void save(PendingRegisterDTO dto) {
      redisTemplate.opsForValue()
        .set(PREFIX + dto.email(), dto, 5, TimeUnit.MINUTES);
    }

    public PendingRegisterDTO find(String email) {
      String key = PREFIX + email;
      Object cache = redisTemplate.opsForValue().get(key);
      return (PendingRegisterDTO) cache;
    }

    public void delete(String email) {
      String key = PREFIX + email;
      redisTemplate.opsForValue().getAndDelete(key);
    }
}
