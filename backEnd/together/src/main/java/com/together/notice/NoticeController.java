package com.together.notice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<NoticeEntity> createNotice(@RequestParam Long userId, @RequestParam Long projectId, @RequestBody NoticeDTO noticeDTO) {
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
    @GetMapping("/all-notice/{projectId}")
    public ResponseEntity<List<NoticeResponseDto>> getNoticesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(noticeService.getNoticesByProject(projectId));
    }

    //특정 공지사항 조회
    @GetMapping("notice/{noticeId}")
    public ResponseEntity<NoticeResponseDto> getNoticeById(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNoticeById(noticeId));
    }

}
