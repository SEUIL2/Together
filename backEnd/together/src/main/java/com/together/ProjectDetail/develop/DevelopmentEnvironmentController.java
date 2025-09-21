package com.together.ProjectDetail.develop;

import com.together.ProjectDetail.develop.dto.DevelopmentEnvironmentRequestDto;
import com.together.ProjectDetail.develop.dto.DevelopmentEnvironmentResponseDto;
import com.together.project.ProjectEntity; // ProjectEntity 경로
import com.together.util.customAnnotation.CurrentProject; // CurrentProject 어노테이션 경로
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev-env") // <<-- 경로를 단순하게 변경
public class DevelopmentEnvironmentController {

    private final DevelopmentEnvironmentService developmentEnvironmentService;

    /**
     * 1. 개발 환경 정보 저장 (POST)
     */
    @PostMapping
    public ResponseEntity<Long> save(
            @CurrentProject Long projectId,
            @RequestBody DevelopmentEnvironmentRequestDto requestDto) {
        Long savedId = developmentEnvironmentService.save(projectId, requestDto); // <<-- 그대로 projectId 전달
        return new ResponseEntity<>(savedId, HttpStatus.CREATED);
    }

    /**
     * 2. 개발 환경 정보 수정 (PUT)
     */
    @PutMapping("/{devEnvId}")
    public ResponseEntity<Long> update(
            @PathVariable Long devEnvId,
            @RequestBody DevelopmentEnvironmentRequestDto requestDto) {
        Long updatedId = developmentEnvironmentService.update(devEnvId, requestDto);
        return ResponseEntity.ok(updatedId);
    }

    /**
     * 3. 개발 환경 정보 삭제 (DELETE)
     */
    @DeleteMapping("/{devEnvId}")
    public ResponseEntity<Void> delete(@PathVariable Long devEnvId) {
        developmentEnvironmentService.delete(devEnvId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 4-1. 개발 환경 정보 개별 조회 (GET)
     */
    @GetMapping("/{devEnvId}")
    public ResponseEntity<DevelopmentEnvironmentResponseDto> findById(@PathVariable Long devEnvId) {
        DevelopmentEnvironmentResponseDto responseDto = developmentEnvironmentService.findById(devEnvId);
        return ResponseEntity.ok(responseDto);
    }


    /**
     * 4-2. 현재 프로젝트의 모든 개발 환경 정보 조회 (GET)
     */
    @GetMapping
    public ResponseEntity<List<DevelopmentEnvironmentResponseDto>> findAllByCurrentProject(
            @CurrentProject Long projectId) { // <<-- ProjectEntity에서 Long 타입으로 수정
        List<DevelopmentEnvironmentResponseDto> responseDtos = developmentEnvironmentService.findAllByProjectId(projectId); // <<-- 그대로 projectId 전달
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * 5. 개발 언어로 검색 (GET)
     */
    @GetMapping("/search")
    public ResponseEntity<List<DevelopmentEnvironmentResponseDto>> searchByDevLanguage(
            @RequestParam("lang") String keyword) {
        List<DevelopmentEnvironmentResponseDto> responseDtos = developmentEnvironmentService.searchByDevLanguage(keyword);
        return ResponseEntity.ok(responseDtos);
    }
}