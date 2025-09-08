package com.together.ProjectDetail.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UnitTestRowResponseDto {
    private Long id;                  // 행 PK
    private String testId;            // 테스트 식별자
    private String methodName;        // 메서드명
    private String caseDesc;          // 케이스 설명
    private String expectedResult;    // 기대 결과
    private String actualResult;      // 실제 결과
    private String inputs;            // 입력 값
    private String caseType;          // 유형
    private String linkedIntegrationId; // 연결된 통합 테스트 ID
    private boolean completed;        // 완료 여부
    private String authorName;        // 작성자 이름
    private LocalDateTime createdAt;  // 작성일
    private LocalDateTime updatedAt;  // 수정일
}