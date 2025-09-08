package com.together.ProjectDetail.test.dto;

import lombok.Data;

@Data
public class IntegrationTestRowRequestDto {
    // 테스트 식별자 (예: SIT_001)
    private String testId;

    // 모듈 이름 (예: "주문 + 결제")
    private String moduleName;

    // 테스트 시나리오 설명
    private String scenario;

    // 기대 결과
    private String expected;

    // 실제 결과 (성공/실패 여부 포함)
    private String result;

    // 비고 (예: 발견된 버그 ID, 참고사항)
    private String note;

    // 완료 여부 (체크박스)
    private Boolean completed;
}