package com.together.ProjectDetail.test;

import com.together.ProjectDetail.test.dto.IntegrationTestRowRequestDto;
import com.together.ProjectDetail.test.dto.IntegrationTestRowResponseDto;
// ↓ 실제 프로젝트 경로로 변경 필요
import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 통합 테스트(Integration) 표 컨트롤러
 * - 프로젝트 ID 자동 주입(@CurrentProject)
 * - 작성자 @AuthenticationPrincipal
 */
@RestController
@RequestMapping("/api/test-rows/integration")
@RequiredArgsConstructor
public class IntegrationTestRowController {

    private final IntegrationTestRowService service;

    // 행 생성
    @PostMapping("/create")
    public ResponseEntity<IntegrationTestRowResponseDto> create(
            @CurrentProject Long projectId,
            @AuthenticationPrincipal UserDetailsImpl me,
            @RequestBody IntegrationTestRowRequestDto dto
    ) {
        Long authorId = me.getUser().getUserId();
        return ResponseEntity.ok(service.create(projectId, authorId, dto));
    }

    // 행 단건 조회(옵션)
    @GetMapping("/{rowId}")
    public ResponseEntity<IntegrationTestRowResponseDto> get(@PathVariable Long rowId) {
        return ResponseEntity.ok(service.get(rowId));
    }

    // 행 수정
    @PutMapping("/{rowId}")
    public ResponseEntity<IntegrationTestRowResponseDto> update(
            @PathVariable Long rowId,
            @RequestBody IntegrationTestRowRequestDto dto
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
    public ResponseEntity<List<IntegrationTestRowResponseDto>> list(@CurrentProject Long projectId) {
        return ResponseEntity.ok(service.listByProject(projectId));
    }

    // 완료 여부 토글
    @PatchMapping("/{rowId}/toggle")
    public ResponseEntity<IntegrationTestRowResponseDto> toggle(@PathVariable Long rowId) {
        return ResponseEntity.ok(service.toggleCompleted(rowId));
    }
}