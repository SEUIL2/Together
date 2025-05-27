package com.together.user.professor.projectmemo;

import com.together.user.professor.projectmemo.dto.ProfessorProjectMemoDto;
import com.together.systemConfig.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 교수 프로젝트 메모 관리용 컨트롤러
 */
@RestController
@RequestMapping("/memos")
@RequiredArgsConstructor
public class ProfessorProjectMemoController {

    private final ProfessorProjectMemoService memoService;

    // ✅ 메모 작성
    @PostMapping
    public ResponseEntity<ProfessorProjectMemoDto> createMemo(
            @RequestParam Long projectId,
            @RequestBody String content,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ProfessorProjectMemoDto memo = memoService.createMemo(userDetails.getUser().getUserId(), projectId, content);
        return ResponseEntity.ok(memo);
    }

    // ✅ 프로젝트별 메모 목록 조회
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProfessorProjectMemoDto>> getProjectMemos(@PathVariable Long projectId) {
        List<ProfessorProjectMemoDto> list = memoService.getMemosByProject(projectId);
        return ResponseEntity.ok(list);
    }

    // ✅ 메모 수정
    @PutMapping("/{memoId}")
    public ResponseEntity<ProfessorProjectMemoDto> updateMemo(
            @PathVariable Long memoId,
            @RequestBody String content
    ) {
        ProfessorProjectMemoDto updated = memoService.updateMemo(memoId, content);
        return ResponseEntity.ok(updated);
    }

    // ✅ 메모 삭제
    @DeleteMapping("/{memoId}")
    public ResponseEntity<String> deleteMemo(@PathVariable Long memoId) {
        memoService.deleteMemo(memoId);
        return ResponseEntity.ok("메모가 삭제되었습니다.");
    }
}
