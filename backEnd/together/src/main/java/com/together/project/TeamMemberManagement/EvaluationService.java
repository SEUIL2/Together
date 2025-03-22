package com.together.project.TeamMemberManagement;

import com.together.project.TeamMemberManagement.dto.EvaluationResponseDto;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // ✅ 평가 생성
    @Transactional
    public EvaluationResponseDto createEvaluation(Long evaluatorId, Long evaluateeId, Long projectId, int score, String comment, LocalDateTime evaluationDate) {
        UserEntity evaluator = userRepository.findById(evaluatorId)
                .orElseThrow(() -> new RuntimeException("평가자를 찾을 수 없습니다."));
        UserEntity evaluatee = userRepository.findById(evaluateeId)
                .orElseThrow(() -> new RuntimeException("평가 대상을 찾을 수 없습니다."));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        EvaluationEntity evaluation = new EvaluationEntity();
        evaluation.setEvaluator(evaluator);
        evaluation.setEvaluatee(evaluatee);
        evaluation.setProject(project);
        evaluation.setScore(score);
        evaluation.setComment(comment);
        evaluation.setEvaluationDate(evaluationDate != null ? evaluationDate : LocalDateTime.now());

        evaluationRepository.save(evaluation);

        return mapToDto(evaluation);
    }

    // ✅ 평가 수정
    @Transactional
    public EvaluationResponseDto updateEvaluation(Long evaluationId, int score, String comment, LocalDateTime evaluationDate) {
        EvaluationEntity evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("평가 정보를 찾을 수 없습니다."));

        // ✅ 수정 내용 반영
        evaluation.setScore(score);
        evaluation.setComment(comment);
        evaluation.setEvaluationDate(evaluationDate != null ? evaluationDate : LocalDateTime.now());

        evaluationRepository.save(evaluation);  // ✅ 반드시 저장 호출

        return mapToDto(evaluation);
    }

    // ✅ 평가 삭제
    @Transactional
    public void deleteEvaluation(Long evaluationId) {
        EvaluationEntity evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("평가 정보를 찾을 수 없습니다."));

        evaluationRepository.delete(evaluation);
    }

    // ✅ 특정 사용자의 평가 조회
    public List<EvaluationResponseDto> getEvaluationsForUser(Long userId) {
        UserEntity evaluatee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
        return evaluationRepository.findByEvaluatee(evaluatee).stream()
                .map(this::mapToDto)
                .toList();
    }

    // ✅ DTO 매핑 로직
    private EvaluationResponseDto mapToDto(EvaluationEntity evaluation) {
        return new EvaluationResponseDto(
                evaluation.getEvaluationId(),
                evaluation.getEvaluator().getUserName(),
                evaluation.getEvaluatee().getUserName(),
                evaluation.getProject().getTitle(),
                evaluation.getScore(),
                evaluation.getComment(),
                evaluation.getEvaluationDate()
        );
    }
}