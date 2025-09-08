package com.together.ProjectDetail.test;


import com.together.ProjectDetail.test.dto.UnitTestRowRequestDto;
import com.together.ProjectDetail.test.dto.UnitTestRowResponseDto;
// ↓ 실제 프로젝트 경로로 변경 필요
import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 단위 테스트(Unit) 표 컨트롤러
 * - 프로젝트 ID는 @CurrentProject로 자동 주입
 * - 작성자는 @AuthenticationPrincipal로 현재 로그인 유저 사용
 */
@RestController
@RequestMapping("/api/test-rows/unit")
@RequiredArgsConstructor
public class UnitTestRowController {

    private final UnitTestRowService service;

    // 행 생성
    @PostMapping("/create")
    public ResponseEntity<UnitTestRowResponseDto> create(
            @CurrentProject Long projectId,
            @AuthenticationPrincipal UserDetailsImpl me,
            @RequestBody UnitTestRowRequestDto dto
    ) {
        Long authorId = me.getUser().getUserId();
        return ResponseEntity.ok(service.create(projectId, authorId, dto));
    }

    // 행 단건 조회(옵션)
    @GetMapping("/{rowId}")
    public ResponseEntity<UnitTestRowResponseDto> get(@PathVariable Long rowId) {
        return ResponseEntity.ok(service.get(rowId));
    }

    // 행 수정
    @PutMapping("/{rowId}")
    public ResponseEntity<UnitTestRowResponseDto> update(
            @PathVariable Long rowId,
            @RequestBody UnitTestRowRequestDto dto
    ) {
        return ResponseEntity.ok(service.update(rowId, dto));
    }

    // 행 삭제 (요청하신 '삭제 기능' 엔드포인트)
    @DeleteMapping("/{rowId}")
    public ResponseEntity<String> delete(@PathVariable Long rowId) {
        service.delete(rowId);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    // 프로젝트별 목록
    @GetMapping("/project")
    public ResponseEntity<List<UnitTestRowResponseDto>> list(@CurrentProject Long projectId) {
        return ResponseEntity.ok(service.listByProject(projectId));
    }

    // 완료 여부 토글
    @PatchMapping("/{rowId}/toggle")
    public ResponseEntity<UnitTestRowResponseDto> toggle(@PathVariable Long rowId) {
        return ResponseEntity.ok(service.toggleCompleted(rowId));
    }
}
