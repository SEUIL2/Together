package com.together.notice;

import com.together.notification.NotificationService;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Builder
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
            //공지사항 출력될때 userName 을 더하기위해 saved 에 저장
            NoticeEntity saved = noticeRepository.save(notice);

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

    // 공지사항 전체 조회
    public List<NoticeResponseDto> getNoticesByProject(Long projectId) {
        return noticeRepository.findByProjectProjectId(projectId).stream()
                .map(this::convertToDto)
                .toList();
    }

    //특정 공지사항 조회
    public NoticeResponseDto getNoticeById(Long noticeId){
        return noticeRepository.findById(noticeId)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을수 없습니다."));
    }

    //userName 을 추출하기 위한 변환 메소드
    private NoticeResponseDto convertToDto(NoticeEntity notice) {
        return NoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdDate(notice.getCreatedDate())
                .authorName(notice.getUser().getUserName()) //여기서 userName 을 받아옴
                .build();
    }

}
