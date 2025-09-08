package com.together.ProjectDetail.test;


import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 단위 테스트(Unit Test) 표의 '한 행' 을 표현하는 엔티티
 * - 필수: project, author, createdAt, completed
 * - 그 외 설명/결과/메모 등은 null 허용
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "unit_test_row")
public class UnitTestRowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    // === 테스트 정보(대부분 null 허용) ===
    @Column(name = "test_id", length = 50, nullable = true)
    private String testId; // 테스트 ID

    @Column(name = "method_name", length = 200, nullable = true)
    private String methodName; // 단위 메서드

    @Column(name = "case_desc", columnDefinition = "TEXT", nullable = true)
    private String caseDesc; // 케이스 설명

    @Column(name = "expected_result", columnDefinition = "TEXT", nullable = true)
    private String expectedResult; // 기대 결과

    @Column(name = "actual_result", columnDefinition = "TEXT", nullable = true)
    private String actualResult; // 실제 결과

    @Column(name = "inputs", columnDefinition = "TEXT", nullable = true)
    private String inputs; // 입력/조건

    @Column(name = "case_type", length = 200, nullable = true)
    private String caseType; // 정상/예외 등

    @Column(name = "linked_integration_id", length = 200, nullable = true)
    private String linkedIntegrationId; // 연결된 통합 테스트 ID(문자열 참조)

    // === 연관 & 메타(필수) ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project; // 소속 프로젝트 (NOT NULL)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author; // 작성자 (NOT NULL)

    @Column(name = "completed", nullable = false)
    private boolean completed; // 완료 여부 (NOT NULL, 기본 false)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성 시각 (NOT NULL)

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt; // 수정 시각 (옵션)
}

