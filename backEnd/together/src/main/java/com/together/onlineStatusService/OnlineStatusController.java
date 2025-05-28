package com.together.onlineStatusService;

import com.together.systemConfig.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OnlineStatusController {

    private final OnlineStatusService onlineStatusService;

    /**
     * 👋 프론트에서 30초마다 호출해야함 - "나 지금 접속 중이야!" 라는 신호를 백엔드에 전달,
     *    접속중이지 않은 유저는 위 API 를 호출할 수 없으므로 키가 생성되지않음
     * @return
     *  "key": "online : 1"
     *  ' 1 ' 이 온라인 상태인 유저의 PK(userId)
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getUserId();
        onlineStatusService.updateOnlineStatus(userId);

        String redisKey = "online : " + userId;

        // 키 값을 JSON 형식으로 응답
        Map<String, String> response = new HashMap<>();
        response.put("key ", redisKey);

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 유저 온라인 여부 확인
     * 다른 사용자가 해당 유저의 온라인 상태를 조회하는 API

     * Redis 에 online:{userId}라는 키가 있으면 → 온라인
     * 없으면 → 오프라인
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<Boolean> getOnlineStatus(@PathVariable Long userId) {
        boolean isOnline = onlineStatusService.isOnline(userId);
        return ResponseEntity.ok(isOnline);
    }

}