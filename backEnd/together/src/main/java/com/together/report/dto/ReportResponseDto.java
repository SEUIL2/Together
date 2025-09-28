// backEnd/together/src/main/java/com/together/report/dto/ReportResponseDto.java
package com.together.report.dto;

import com.together.report.ReportEntity;
import com.together.report.ReportCategory;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 보고서 조회 응답 DTO
 */
@Getter
@Setter
public class ReportResponseDto {

    // 보고서 ID
    private Long id;

    // 보고서 제목
    private String title;

    // 보고서 기간
    private String period;

    // 카테고리
    private ReportCategory category;

    // 금주 진행 내용
    private String weeklyProgress;

    // 문제점 및 해결방안
    private String problemsAndSolutions;

    // 향후 계획
    private String futurePlans;

    //교수 피드백
    private String feedback;

    // 작성자 이름
    private String authorName;

    // 생성 시간
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;


    // 엔티티를 DTO로 변환
    public static ReportResponseDto fromEntity(ReportEntity reportEntity) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setId(reportEntity.getId());
        dto.setTitle(reportEntity.getTitle());
        dto.setPeriod(reportEntity.getPeriod());
        dto.setCategory(reportEntity.getCategory());
        dto.setWeeklyProgress(reportEntity.getWeeklyProgress());
        dto.setProblemsAndSolutions(reportEntity.getProblemsAndSolutions());
        dto.setFuturePlans(reportEntity.getFuturePlans());
        dto.setAuthorName(reportEntity.getAuthor().getUserName()); // 작성자 이름 추가
        dto.setFeedback(reportEntity.getFeedback());
        dto.setCreatedAt(reportEntity.getCreatedAt());
        dto.setUpdatedAt(reportEntity.getUpdatedAt());
        return dto;
    }
}