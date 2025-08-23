// src/main/java/com/together/project/worktask/dto/ScheduleUpdateDto.java
package com.together.worktask.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Gantt 그래프 드래그 시 변경된 시작/종료일만 전달받는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ScheduleUpdateDto {

    /** 변경된 시작일 (yyyy-MM-dd 포맷) */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 변경된 종료일 (yyyy-MM-dd 포맷) */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

