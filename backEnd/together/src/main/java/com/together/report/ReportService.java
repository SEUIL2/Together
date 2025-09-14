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

    // 보고서 삭제
    @Transactional
    public void deleteReport(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new IllegalArgumentException("삭제할 보고서를 찾을 수 없습니다. ID: " + reportId);
        }
        reportRepository.deleteById(reportId);
    }
}
