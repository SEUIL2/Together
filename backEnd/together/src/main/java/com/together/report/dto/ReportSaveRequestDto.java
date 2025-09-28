package com.together.report.dto;

import com.together.report.ReportCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 보고서 저장/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ReportSaveRequestDto {

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
}