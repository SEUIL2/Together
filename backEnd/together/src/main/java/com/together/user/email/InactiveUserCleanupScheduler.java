package com.together.user.email;

import com.together.user.UserEntity;
import com.together.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 회원가입시에 이메일이 인증되지 않은 채 만들어진 더미유저 데이터를 삭제하기 위한 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InactiveUserCleanupScheduler {

    private final UserRepository userRepository;

    // 매일 새벽 2시 정각에 실행
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24); // 24시간 이상
        List<UserEntity> inactiveUsers = userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(threshold);

        if (!inactiveUsers.isEmpty()) {
            userRepository.deleteAll(inactiveUsers);
            log.info("삭제된 미인증 사용자 수: {}", inactiveUsers.size());
        }
    }


}
