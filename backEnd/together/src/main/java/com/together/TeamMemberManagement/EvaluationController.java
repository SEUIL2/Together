package com.together.TeamMemberManagement;

import com.together.TeamMemberManagement.dto.EvaluationRequestDto; // ✅ 경로 수정
import com.together.TeamMemberManagement.dto.EvaluationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    // ✅ 평가 생성
    @PostMapping("/create")
    public ResponseEntity<EvaluationResponseDto> createEvaluation(@RequestBody EvaluationRequestDto requestDto) {
        EvaluationResponseDto evaluation = evaluationService.createEvaluation(
                requestDto.getEvaluatorId(),
                requestDto.getEvaluateeId(),
                requestDto.getProjectId(),
                requestDto.getScore(),
                requestDto.getComment(),
                requestDto.getEvaluationDate()
        );

        return ResponseEntity.ok(evaluation);
    }

    // ✅ 평가 수정
    @PutMapping("/update/{evaluationId}")
    public ResponseEntity<EvaluationResponseDto> updateEvaluation(
            @PathVariable Long evaluationId,
            @RequestBody EvaluationRequestDto requestDto) {

        EvaluationResponseDto updatedEvaluation = evaluationService.updateEvaluation(
                evaluationId,
                requestDto.getScore(),
                requestDto.getComment(),
                requestDto.getEvaluationDate()  // 날짜 업데이트 추가
        );

        return ResponseEntity.ok(updatedEvaluation);
    }

    // ✅ 평가 삭제
    @DeleteMapping("/delete/{evaluationId}")
    public ResponseEntity<String> deleteEvaluation(@PathVariable Long evaluationId) {
        evaluationService.deleteEvaluation(evaluationId);
        return ResponseEntity.ok("평가가 성공적으로 삭제되었습니다.");
    }

    // ✅ 특정 사용자의 평가 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EvaluationResponseDto>> getEvaluationsForUser(@PathVariable Long userId) {
        List<EvaluationResponseDto> evaluations = evaluationService.getEvaluationsForUser(userId);
        return ResponseEntity.ok(evaluations);
    }
}
