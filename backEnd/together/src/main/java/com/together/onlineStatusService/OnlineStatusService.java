package com.together.onlineStatusService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OnlineStatusService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "user:online:";

    // Heartbeat 시 업데이트
    public void updateOnlineStatus(Long userId) {
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(key, "online", Duration.ofSeconds(30)); // TTL 30초
    }

    // 온라인 상태 조회
    public boolean isOnline(Long userId) {
        String key = PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
