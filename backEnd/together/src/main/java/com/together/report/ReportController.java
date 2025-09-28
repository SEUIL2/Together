
package com.together.report;

import com.together.report.dto.ReportResponseDto;
import com.together.report.dto.ReportSaveRequestDto;
import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 보고서 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 새 보고서 작성
    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(
            @CurrentProject Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ReportSaveRequestDto requestDto) {
        Long userId = userDetails.getUser().getUserId();
        ReportResponseDto createdReport = reportService.createReport(projectId, userId, requestDto);
        return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    }

    // 현재 프로젝트의 모든 보고서 조회
    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getReportsByProject(@CurrentProject Long projectId) {
        List<ReportResponseDto> reports = reportService.getReportsByProject(projectId);
        return ResponseEntity.ok(reports);
    }

    // 특정 보고서 상세 조회
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDto> getReportById(@PathVariable Long reportId) {
        ReportResponseDto report = reportService.getReportById(reportId);
        return ResponseEntity.ok(report);
    }

    // 보고서 수정
    @PutMapping("/{reportId}")
    public ResponseEntity<ReportResponseDto> updateReport(
            @PathVariable Long reportId,
            @RequestBody ReportSaveRequestDto requestDto) {
        ReportResponseDto updatedReport = reportService.updateReport(reportId, requestDto);
        return ResponseEntity.ok(updatedReport);
    }

    // 보고서 삭제
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 보고서에 교수 피드백을 추가하거나 수정합니다.
     * @param reportId 피드백을 추가할 보고서의 ID
     * @param requestDto 피드백 내용이 담긴 DTO
     * @return 수정된 보고서 정보
     */
    @PostMapping("/{reportId}/feedback")
    public ResponseEntity<ReportResponseDto> addFeedbackToReport(
            @PathVariable Long reportId,
            @RequestBody ReportSaveRequestDto requestDto) {
        ReportResponseDto updatedReport = reportService.addFeedbackToReport(reportId, requestDto);
        return ResponseEntity.ok(updatedReport);
    }

    /**
     * 특정 보고서의 교수 피드백을 삭제합니다.
     * @param reportId 피드백을 삭제할 보고서의 ID
     * @return 성공 시 noContent 응답
     */
    @DeleteMapping("/{reportId}/feedback")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long reportId) {
        reportService.deleteFeedback(reportId);
        return ResponseEntity.noContent().build();
    }
}
