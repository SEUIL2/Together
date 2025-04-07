package com.together.project.ProjectDetail.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 전체 기획 항목을 한 번에 조회할 때 사용하는 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningAllResponseDto {
    private PlanningItemDto motivation;      // 프로젝트 동기
    private PlanningItemDto goal;            // 프로젝트 목표
    private PlanningItemDto requirement;     // 요구사항 정의
    private PlanningItemDto infostructure;   // 정보 구조도
    private PlanningItemDto storyboard;      // 스토리보드
    private PlanningItemDto description;     // 프로젝트 설명
}
