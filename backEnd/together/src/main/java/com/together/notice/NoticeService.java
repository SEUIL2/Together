package com.together.notice;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
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
