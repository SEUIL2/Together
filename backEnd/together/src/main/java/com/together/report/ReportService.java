// backEnd/together/src/main/java/com/together/report/ReportService.java
package com.together.report;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.report.dto.ReportResponseDto;
import com.together.report.dto.ReportSaveRequestDto;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 보고서 관련 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 새 보고서 작성
    @Transactional
    public ReportResponseDto createReport(Long projectId, Long userId, ReportSaveRequestDto requestDto) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId));
        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        ReportEntity newReport = new ReportEntity();
        newReport.setTitle(requestDto.getTitle());
        newReport.setPeriod(requestDto.getPeriod());
        newReport.setCategory(requestDto.getCategory());
        newReport.setWeeklyProgress(requestDto.getWeeklyProgress());
        newReport.setProblemsAndSolutions(requestDto.getProblemsAndSolutions());
        newReport.setFuturePlans(requestDto.getFuturePlans());
        newReport.setProject(project);
        newReport.setAuthor(author);

        ReportEntity savedReport = reportRepository.save(newReport);
        return ReportResponseDto.fromEntity(savedReport);
    }

    // 프로젝트의 모든 보고서 조회
    public List<ReportResponseDto> getReportsByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId);
        }
        List<ReportEntity> reports = reportRepository.findByProject_ProjectId(projectId);
        return reports.stream()
                .map(ReportResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 보고서 상세 조회
    public ReportResponseDto getReportById(Long reportId) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 보고서를 찾을 수 없습니다. ID: " + reportId));
        return ReportResponseDto.fromEntity(report);
    }

    // 보고서 수정
    @Transactional
    public ReportResponseDto updateReport(Long reportId, ReportSaveRequestDto requestDto) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 보고서를 찾을 수 없습니다. ID: " + reportId));

        report.setTitle(requestDto.getTitle());
        report.setPeriod(requestDto.getPeriod());
        report.setCategory(requestDto.getCategory());
        report.setWeeklyProgress(requestDto.getWeeklyProgress());
        report.setProblemsAndSolutions(requestDto.getProblemsAndSolutions());
        report.setFuturePlans(requestDto.getFuturePlans());

        return ReportResponseDto.fromEntity(report); // 더티 체킹으로 자동 업데이트
    }

    // 교수 피드백 추가/수정 (교수용)
    @Transactional
    public ReportResponseDto addFeedbackToReport(Long reportId, ReportSaveRequestDto requestDto) {
        // 1. reportId를 사용하여 보고서 엔티티를 조회합니다.
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 보고서를 찾을 수 없습니다. ID: " + reportId));

        // 2. 요청 DTO에서 피드백 내용만 가져와서 엔티티의 feedback 필드를 업데이트합니다.
        report.setFeedback(requestDto.getFeedback());

        // 3. 변경된 내용이 적용된 엔티티를 DTO로 변환하여 반환합니다. (JPA의 변경 감지 기능으로 자동 저장됩니다)
        return ReportResponseDto.fromEntity(report);
    }

    // 교수 피드백 삭제 (교수용)
    @Transactional
    public void deleteFeedback(Long reportId) {
        // 1. reportId로 해당 보고서 엔티티를 찾습니다.
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 보고서를 찾을 수 없습니다. ID: " + reportId));

        // 2. 해당 보고서의 feedback 필드를 null로 설정하여 피드백을 삭제합니다.
        report.setFeedback(null);

        // 3. 변경된 내용은 트랜잭션이 끝날 때 자동으로 데이터베이스에 반영됩니다.
    }

    // 보고서 삭제
    @Transactional
    public void deleteReport(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new IllegalArgumentException("삭제할 보고서를 찾을 수 없습니다. ID: " + reportId);
        }
        reportRepository.deleteById(reportId);
    }


}
