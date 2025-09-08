package com.together.ProjectDetail.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class IntegrationTestRowResponseDto {
    private Long id;                  // 행 PK
    private String testId;            // 테스트 ID
    private String moduleName;        // 모듈명
    private String scenario;          // 시나리오
    private String expected;          // 기대 결과
    private String result;            // 실제 결과
    private String note;              // 비고
    private boolean completed;        // 완료 여부
    private String authorName;        // 작성자 이름
    private LocalDateTime createdAt;  // 작성일
    private LocalDateTime updatedAt;  // 수정일
}
