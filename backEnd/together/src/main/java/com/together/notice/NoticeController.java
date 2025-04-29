package com.together.notice;

import com.together.systemConfig.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    // 공지사항 작성
    @PostMapping("/create")
    public ResponseEntity<NoticeEntity> createNotice(@RequestParam(required = false) Long projectId,//AOP 를 통해 교수일경우 불러오는값을 사용, 학생일 경우 자동 설정
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @RequestBody NoticeDTO noticeDTO) {

        Long userId = userDetails.getUser().getUserId();

        NoticeEntity createdNotice = noticeService.createNotice(userId, projectId, noticeDTO);
        if (createdNotice != null) {
            return ResponseEntity.ok(createdNotice);
        }
        return ResponseEntity.badRequest().body(null);
    }

    // 공지사항 수정
    @PutMapping("/update/{noticeId}")
    public ResponseEntity<NoticeEntity> updateNotice(@PathVariable Long noticeId, @RequestBody NoticeDTO noticeDTO) {
        NoticeEntity updatedNotice = noticeService.updateNotice(noticeId, noticeDTO);
        if (updatedNotice != null) {
            return ResponseEntity.ok(updatedNotice);
        }
        return ResponseEntity.notFound().build();
    }

    // 공지사항 삭제
    @DeleteMapping("/delete/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long noticeId) {
        boolean deleted = noticeService.deleteNotice(noticeId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 공지사항 조회
    @GetMapping("/all-notice")
    public ResponseEntity<List<NoticeResponseDto>> getNoticesByProject(
            @RequestParam(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {//AOP 를 통해 교수일경우 불러오는값을 사용, 학생일 경우 자동 설정

        return ResponseEntity.ok(noticeService.getNoticesByProject(projectId));
    }

    //특정 공지사항 조회
    @GetMapping("notice/{noticeId}")
    public ResponseEntity<NoticeResponseDto> getNoticeById(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNoticeById(noticeId));
    }

}
