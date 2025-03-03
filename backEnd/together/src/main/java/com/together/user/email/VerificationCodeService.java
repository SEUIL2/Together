package com.together.user.email;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationCodeService { //Redis를 이용한 인증 코드 저장

    private final StringRedisTemplate redisTemplate;
    private final static long CODE_EXPIRATION_TIME = 5; // 코드 유효 시간 (5분)

    // 인증 코드 저장
    public void saveVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(email, code, CODE_EXPIRATION_TIME, TimeUnit.MINUTES);
    }

    // 인증 코드 확인
    public boolean verifyCode(String email, String inputCode) {
        String storedCode = redisTemplate.opsForValue().get(email);
        return storedCode != null && storedCode.equals(inputCode);
    }

}
