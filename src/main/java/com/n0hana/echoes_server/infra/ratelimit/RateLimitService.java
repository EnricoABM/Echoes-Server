package com.n0hana.echoes_server.infra.ratelimit;

import io.github.bucket4j.*;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String PREFIX = "ratelimit:";

    public Bucket resolveBucket(String key) {
      String keyBucket = PREFIX + key;
      Object cache = redisTemplate.opsForValue().get(keyBucket);
      if (cache == null) {
        cache = createBucket();
      }
      return (Bucket) cache;
    }

    private Bucket createBucket() {
        return Bucket.builder()
              .addLimit(limit -> limit
              .capacity(5)
                .refillIntervally(1, Duration.ofMinutes(1))
              )
              .build();
    }
}
