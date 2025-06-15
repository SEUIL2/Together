package com.together.project.ProjectDetail.test.dto;

import lombok.Data;
import java.util.List;

/**
 * 프로젝트 전체 테스트(단위+통합) 응답 DTO
 */
@Data
public class ProjectTestTablesDto {
    private List<TestTableDto> tables;    // 단위/통합 표 두 개 리스트
}
