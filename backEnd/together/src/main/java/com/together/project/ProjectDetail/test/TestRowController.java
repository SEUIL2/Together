package com.together.project.ProjectDetail.test;

import com.together.project.ProjectDetail.test.dto.TestRowDto;
import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 테스트(단위/통합) 표 행 관리 컨트롤러 (프로젝트ID, 작성자ID 자동 추출)
 */
@RestController
@RequestMapping("/api/test-rows")
@RequiredArgsConstructor
public class TestRowController {

    private final TestRowService testRowService;

    /** (1) 테스트 행 등록 */
    @PostMapping("/create")
    public ResponseEntity<TestRowDto> createTestRow(
            @CurrentProject Long projectId, // ← 자동 추출!
            @AuthenticationPrincipal UserDetailsImpl userDetails, // ← 로그인 유저 자동
            @RequestParam TestTableType tableType,
            @RequestParam String itemName,
            @RequestParam String description
    ) {
        Long authorId = userDetails.getUser().getUserId();
        TestRowDto dto = testRowService.createTestRow(projectId, tableType, itemName, description, authorId);
        return ResponseEntity.ok(dto);
    }

    /** (2) 테스트 행 수정 */
    @PutMapping("/update/{rowId}")
    public ResponseEntity<TestRowDto> updateTestRow(
            @PathVariable Long rowId,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean completed
    ) {
        TestRowDto dto = testRowService.updateTestRow(rowId, itemName, description, completed);
        return ResponseEntity.ok(dto);
    }

    /** (3) 테스트 행 삭제 */
    @DeleteMapping("/delete/{rowId}")
    public ResponseEntity<String> deleteTestRow(@PathVariable Long rowId) {
        testRowService.deleteTestRow(rowId);
        return ResponseEntity.ok("테스트 행이 삭제되었습니다.");
    }

    /** (4) 완료여부 토글 */
    @PatchMapping("/toggle/{rowId}")
    public ResponseEntity<TestRowDto> toggleCompleted(@PathVariable Long rowId) {
        TestRowDto dto = testRowService.toggleCompleted(rowId);
        return ResponseEntity.ok(dto);
    }

    /** (5) 프로젝트+유형별 테스트 행 전체 조회 */
    @GetMapping("/list")
    public ResponseEntity<List<TestRowDto>> getRowsByProjectAndType(
            @CurrentProject Long projectId, // ← 자동 추출!
            @RequestParam TestTableType tableType
    ) {
        List<TestRowDto> list = testRowService.getRowsByProjectAndType(projectId, tableType);
        return ResponseEntity.ok(list);
    }

    /** (6) 단일 행 조회 */
    @GetMapping("/{rowId}")
    public ResponseEntity<TestRowDto> getTestRowById(@PathVariable Long rowId) {
        TestRowDto dto = testRowService.getTestRowById(rowId);
        return ResponseEntity.ok(dto);
    }
}