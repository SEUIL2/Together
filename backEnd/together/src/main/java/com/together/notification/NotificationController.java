package com.together.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     *  안 읽은 알림 목록 조회
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 리스트
     * 프론트: 종 아이콘 클릭 시 이 API 호출
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationEntity>> getUnread(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    /**
     *  전체 알림 내역 조회
     * @param userId 사용자 ID
     * @return 알림 전체 목록 (최신순)
     * 프론트: 알림 내역 페이지에서 사용
     */
    @GetMapping("/all")
    public ResponseEntity<List<NotificationEntity>> getAll(@RequestParam Long userId) {
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
