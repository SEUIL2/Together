package com.together.notification;

import com.together.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     *  알림 생성 (DB에 저장)
     * @param userId 알림 받을 사용자 ID
     * @param message 알림 메시지 (ex. "새 공지사항이 등록되었습니다.")
     * @param linkUrl 클릭 시 이동할 페이지 경로 (ex. "/notices/5")
     *  현재 생성된 자동 알림
     * 1. 프로젝트 초대 _ ProjectService.java -> inviteUserToProject() _ 테스트완료
     * 2. 공지사항 등록 _ NoticeService.java -> createNotice() _ 테스트 완료
     * 3. 투표 등록 _ VoteService.java -> createVote()
     * 예정된 자동알림
     * 4. 작업 할당
     * 5. 교수 피드백
     */
    public void sendNotification(Long userId, String message, String linkUrl) {
        userRepository.findById(userId).ifPresent(user -> {
            NotificationEntity noti = NotificationEntity.builder()
                    .message(message)
                    .linkUrl(linkUrl)
                    .isRead(false)
                    .createdAt(new Date())
                    .receiver(user)
                    .build();
            log.info("알림 생성됨: {}", noti);
            notificationRepository.save(noti);
        });
    }

    /**
     *  읽지 않은 알림 목록 조회
     * (상단 알림 종 아이콘에 표시되는 내용)
     */
    public List<NotificationEntity> getUnreadNotifications(Long userId) {

        return notificationRepository.findByReceiverUserIdAndIsReadFalse(userId);
    }

    /**
     *  모든 알림 내역 조회 (최신순)
     * (알림 내역 페이지에서 사용)
     */
    public List<NotificationEntity> getAllNotifications(Long userId) {

        return notificationRepository.findByReceiverUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     *  알림 읽음 처리
     * (알림 클릭 시 호출)
     */
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(noti -> {
            noti.setRead(true);
            notificationRepository.save(noti);
        });
    }

    /**
     *  알림 삭제
     * (알림 내역에서 사용자가 삭제 버튼 눌렀을 때)
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}