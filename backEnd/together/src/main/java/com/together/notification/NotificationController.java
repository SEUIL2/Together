package com.together.notification;

import com.together.systemConfig.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /*
     * 알림 자동 생성 메소드는 NotificationService.java 에 있고
     * 각 자동생성 필요한 시스템에 코드가 추가된 형태로 작동됨
     */

    /**
     *  안 읽은 알림 목록 조회
     * @return 읽지 않은 알림 리스트
     * 프론트: 종 아이콘 클릭 시 이 API 호출
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationEntity>> getUnread(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getUserId();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    /**
     *  전체 알림 내역 조회
     * @return 알림 전체 목록 (최신순)
     * 프론트: 알림 내역 페이지에서 사용
     */
    @GetMapping("/all")
    public ResponseEntity<List<NotificationEntity>> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getUserId();
        return ResponseEntity.ok(notificationService.getAllNotifications(userId));
    }

    /**
     *  알림 읽음 처리
     * @param id 알림 ID
     * 프론트: 알림 클릭하면 호출 (읽음 처리 후 이동)
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        log.info("알림 읽음 처리, id: {}", id);
        return ResponseEntity.ok().build();
    }

    /**
     *  알림 삭제
     * @param id 알림 ID
     * 프론트: 알림 내역에서 삭제 버튼 클릭 시 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
