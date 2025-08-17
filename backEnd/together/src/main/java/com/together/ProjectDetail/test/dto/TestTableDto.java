package com.together.ProjectDetail.test.dto;

import lombok.Data;
import java.util.List;

/**
 * 단위/통합 테스트 표 리스트 DTO
 */
@Data
public class TestTableDto {
    private String tableType;             // "UNIT" 또는 "INTEGRATION"
    private List<TestRowDto> rows;        // 표의 모든 행 리스트
}
