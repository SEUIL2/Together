package com.together.generatedCodeAI;

import com.together.generatedCodeAI.DTO.ClassDiagramCodeRequest;
import com.together.generatedCodeAI.DTO.ErdCodeRequest;
import com.together.generatedCodeAI.DTO.GeneratedCodeResponse;
import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai-code")
@RequiredArgsConstructor
public class CodeGenerationController {

    private final CodeGenerationService service;

    /**
     * 클래스 다이어그램 기반으로 코드 생성 요청
     * - 프론트에서 전달한 클래스 다이어그램 기반 JSON을 바탕으로
     * - 선택한 언어("java-SpringBoot", "nodeJS", "python-FastAPI", "csharp", "kotlin")에 대한 백엔드 코드를 생성 - 프론트에서 해당 항목들로 선택할 수 있게 해야됌
     * - DB에 자동 저장됨
     */
    @PostMapping("/generate/class")
    public ResponseEntity<String> generateFromClass(
            @RequestBody ClassDiagramCodeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @CurrentProject Long project) {
        service.generateFromClass(request, userDetails.getUser(), project);
        return ResponseEntity.ok("AI 코드 생성에 성공했습니다.");
    }

    /**
     * ER 다이어그램 기반으로 코드 생성 요청
     * - 위와 동일하지만 ERD 기반으로 작동
     */
    @PostMapping("/generate/erd")
    public ResponseEntity<String> generateFromErd(
            @RequestBody ErdCodeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @CurrentProject Long project) {
        service.generateFromErd(request, userDetails.getUser(), project);
        return ResponseEntity.ok("AI 코드 생성에 성공했습니다.");
    }

    /**
     * 현재 프로젝트에 생성된 코드 목록 전체 조회
     * - 전체 생성 이력 보여줄 때 사용 (왼쪽 리스트 등)
     */
    @GetMapping
    public ResponseEntity<List<GeneratedCodeResponse>> getAll(
            @CurrentProject Long project) {
        return ResponseEntity.ok(service.getAll(project));
    }

    /**
     * 생성된 코드 상세 조회
     * - 리스트에서 특정 코드 선택 시 상세 내용을 확인하기 위함
     */
    @GetMapping("/{id}")
    public ResponseEntity<GeneratedCodeResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    /**
     * 생성된 코드 삭제 (작성자 본인만 삭제 가능)
     * - 교수는 삭제 불가능 (조회만 가능)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        service.delete(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

}
