package com.together.project.ProjectDetail.test.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 테스트(단위/통합) 표의 한 행(상세 항목) DTO
 */
@Data
public class TestRowDto {
    private Long id;            // 행 PK
    private String itemName;    // 테스트 항목명
    private String description; // 설명
    private String authorName;  // 작성자 이름
    private LocalDate createdDate; // 작성일
    private boolean completed;  // 완료여부(체크박스)
}