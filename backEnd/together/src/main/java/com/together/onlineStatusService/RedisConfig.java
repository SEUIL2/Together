package com.together.onlineStatusService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration // 스프링이 이 클래스를 설정 클래스임을 인식하도록 함
public class RedisConfig {

    // Redis 연결을 위한 커넥션 팩토리 빈 생성
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(); // Lettuce는 비동기 논블로킹 방식의 Redis 클라이언트
    }

    // Redis 작업을 수행할 RedisTemplate 빈 생성
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        // 커넥션 팩토리 설정 (위에서 만든 redisConnectionFactory 사용)
        template.setConnectionFactory(redisConnectionFactory());

        // Key를 문자열 형태로 직렬화
        template.setKeySerializer(new StringRedisSerializer());

        // Value도 문자열 형태로 직렬화
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
