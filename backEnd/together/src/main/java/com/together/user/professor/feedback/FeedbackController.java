package com.together.user.professor.feedback;

import com.together.systemConfig.UserDetailsImpl;
import com.together.user.UserEntity;
import com.together.user.professor.feedback.DTO.CreateFeedbackRequest;
import com.together.user.professor.feedback.DTO.FeedbackDto;
import com.together.user.professor.feedback.DTO.FeedbackSummaryDto;
import com.together.util.customAnnotation.CurrentProject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;

    //피드백 생성
    @PostMapping("/create")
    public ResponseEntity<?> createFeedback(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CreateFeedbackRequest dto
    ) {
        // 🔒 교수인지 확인
        if (userDetails.getUser().getRole() != UserEntity.UserRole.PROFESSOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "접근 권한이 없습니다. 교수만 피드백을 작성할 수 있습니다."));
        }

        FeedbackEntity feedback = feedbackService.createFeedback(dto, userDetails.getUser().getUserId());

        FeedbackDto response = new FeedbackDto(
                feedback.getFeedbackId(),
                feedback.getProject().getProjectId(),
                feedback.getPage(),
                feedback.getX(),
                feedback.getY(),
                feedback.getText(),
                userDetails.getUsername(), // author
                feedback.getCreatedAt(),
                feedback.getIsRead(),
                feedback.getCategory()
        );
        return ResponseEntity.ok(response);
    }

    //전체 피드백 조회
    @GetMapping("/my")
    public ResponseEntity<List<FeedbackSummaryDto>> getMyFeedbacks(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @CurrentProject Long projectId) {
        Long userId = userDetails.getUser().getUserId();
        List<FeedbackSummaryDto> feedbacks = feedbackService.getFeedbacksByUser(userId, projectId);
        return ResponseEntity.ok(feedbacks);
    }

    //페이지 피드백 조회
    @GetMapping("/project")
    public ResponseEntity<?> getFeedbacksByProject(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String page,
            @CurrentProject Long projectId
    ) {
        List<FeedbackDto> feedbacks = feedbackService.getFeedbacks(projectId, page, userDetails.getUser().getUserId());
        return ResponseEntity.ok(feedbacks);
    }

    //특정 피드백 조회
    @GetMapping("/{feedbackId}")
    public ResponseEntity<?> getFeedbackById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long feedbackId,
            @CurrentProject Long projectId)
    {
        FeedbackEntity feedback = feedbackService.getFeedbackById(userDetails.getUser().getUserId(), feedbackId, projectId);
        FeedbackDto response = new FeedbackDto(
                feedback.getFeedbackId(),
                feedback.getProject().getProjectId(),
                feedback.getPage(),
                feedback.getX(),
                feedback.getY(),
                feedback.getText(),
                feedback.getAuthor().getUserName(),
                feedback.getCreatedAt(),
                feedback.getIsRead(),
                feedback.getCategory()
        );
        return ResponseEntity.ok(response);
    }

    //피드백 읽음 처리
    @PostMapping("/{feedbackId}/read")
    public ResponseEntity<?> markFeedbackAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long feedbackId
    ) {
        feedbackService.markFeedbackAsRead(userDetails.getUser().getUserId(), feedbackId);

        return ResponseEntity.ok("피드백 읽음 처리 완료");
    }

    //피드백 안읽음 처리
    @DeleteMapping("/{feedbackId}/read")
    public ResponseEntity<?> markFeedbackAsUnread(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long feedbackId
    ) {
        feedbackService.markFeedbackAsDeleted(userDetails.getUser().getUserId(), feedbackId);
        return ResponseEntity.ok().body("피드백 읽음 기록이 삭제되었습니다.");
    }

    //피드백 삭제기능
    //교수와 학생이 자기가 속한(관리하는) 프로젝트의 피드백을 삭제할수있음
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<?> deleteFeedback(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @CurrentProject Long projectId,
            @PathVariable Long feedbackId) {

        FeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        if(userDetails.getUser().getRole() == UserEntity.UserRole.STUDENT ) {
            if (feedback.getProject().getProjectId().equals(projectId) != userDetails.getUser().getUserId().equals(projectId)) {
                throw new SecurityException("본인이 속한 프로젝트의 피드백만 삭제할 수 있습니다.");
            }
        } else if (userDetails.getUser().getRole() == UserEntity.UserRole.PROFESSOR) {
            Long feedbackProjectId = feedback.getProject().getProjectId();
            if (!Objects.equals(feedbackProjectId, projectId)) {
                throw new SecurityException("본인이 관리하는 프로젝트의 피드백만 삭제할 수 있습니다.");
            }
        }

        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.ok().body("피드백이 삭제되었습니다.");
    }

}
