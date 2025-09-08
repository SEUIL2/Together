package com.together.ProjectDetail.test.dto;

import lombok.Data;

@Data
public class UnitTestRowRequestDto {
    // 테스트 식별자 (예: UT_001)
    private String testId;

    // 테스트할 메서드 이름 (예: OrderService.calculatePrice)
    private String methodName;

    // 케이스 설명
    private String caseDesc;

    // 기대 결과
    private String expectedResult;

    // 실제 실행 결과
    private String actualResult;

    // 입력 값 / 조건
    private String inputs;

    // 케이스 유형 (정상 / 예외)
    private String caseType;

    // 연결된 통합 테스트 ID (예: SIT_001)
    private String linkedIntegrationId;

    // 완료 여부 체크 (체크박스)
    private Boolean completed;
}