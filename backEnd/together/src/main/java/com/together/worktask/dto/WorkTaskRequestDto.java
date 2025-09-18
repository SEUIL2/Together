// src/main/java/com/together/project/worktask/dto/WorkTaskRequestDto.java
package com.together.worktask.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.together.report.ReportCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * WorkTask 생성·수정용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkTaskRequestDto {
    /** 작업 제목 */
    private String title;

    /** 작업 상세 설명 */
    private String description;

    /** 시작일 (yyyy-MM-dd) */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 종료일 (yyyy-MM-dd) */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 담당자 userId (프로젝트 멤버) */
    private Long assignedUserId;

    /** 상태 (PENDING, IN_PROGRESS, COMPLETED) */
    private String status;

    /** 상위 작업 ID (하위 작업 생성 시) */
    private Long parentTaskId;

    private ReportCategory category; //카테고리
}

