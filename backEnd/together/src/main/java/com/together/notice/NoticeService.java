package com.together.notice;

import com.together.notification.NotificationService;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.project.ProjectService;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotificationService notificationService;

    // 공지사항 작성
    public NoticeEntity createNotice(Long userId, Long projectId, NoticeDTO noticeDTO) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);

        if (userOptional.isPresent() && projectOptional.isPresent()) {
            UserEntity user = userOptional.get();
            ProjectEntity project = projectOptional.get();

            NoticeEntity notice = new NoticeEntity();
            notice.setTitle(noticeDTO.getTitle());
            notice.setContent(noticeDTO.getContent());
            notice.setUser(user);
            notice.setProject(project);

            // ⭐ 알림 전송 코드 추가
            List<UserEntity> projectUsers = project.getUsers();
            if (projectUsers != null) {
                for (UserEntity notificationUser : projectUsers) {
                    notificationService.sendNotification(
                            notificationUser.getUserId(),
                            "공지사항 등록",
                            notice.getTitle() + " 공지사항이 등록되었습니다."
                    );
                }
            }

            return noticeRepository.save(notice);
        }
        return null;  // User or Project not found
    }

    // 공지사항 수정
    public NoticeEntity updateNotice(Long noticeId, NoticeDTO noticeDTO) {
        Optional<NoticeEntity> noticeOptional = noticeRepository.findById(noticeId);

        if (noticeOptional.isPresent()) {
            NoticeEntity notice = noticeOptional.get();
            notice.setTitle(noticeDTO.getTitle());
            notice.setContent(noticeDTO.getContent());
            return noticeRepository.save(notice);
        }

        return null;  // Notice not found
    }

    // 공지사항 삭제
    public boolean deleteNotice(Long noticeId) {
        Optional<NoticeEntity> noticeOptional = noticeRepository.findById(noticeId);

        if (noticeOptional.isPresent()) {
            noticeRepository.delete(noticeOptional.get());
            return true;
        }

        return false;  // Notice not found
    }

    // 공지사항 조회
    public List<NoticeEntity> getNoticesByProject(Long projectId) {
        return noticeRepository.findByProjectProjectId(projectId);
    }
}
