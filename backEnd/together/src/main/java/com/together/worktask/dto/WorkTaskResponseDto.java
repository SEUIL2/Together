// src/main/java/com/together/project/worktask/dto/WorkTaskResponseDto.java
package com.together.worktask.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.together.report.ReportCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * WorkTask 조회용 DTO
 */
@Getter
@Setter
@AllArgsConstructor
public class WorkTaskResponseDto {
    /** 작업 ID */
    private Long id;

    /** 작업 제목 */
    private String title;

    /** 작업 상세 설명 */
    private String description;

    /** 시작일 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 종료일 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 담당자 ID */
    private Long assignedUserId;

    /** 담당자 이름 (Optional) */
    private String assignedUserName;

    /** 상태 */
    private String status;

    /** 상위 작업 ID */
    private Long parentTaskId;

    /** 하위 작업들 (재귀 구조) */
    private List<WorkTaskResponseDto> childTasks;

    private ReportCategory category; //카테고리
}
