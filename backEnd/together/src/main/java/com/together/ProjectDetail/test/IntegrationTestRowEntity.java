package com.together.ProjectDetail.test;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 통합 테스트(Integration Test) 표의 '한 행' 을 표현하는 엔티티
 * - 필수: project, author, createdAt, completed
 * - 나머지 컬럼은 대부분 null 허용
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "integration_test_row")
public class IntegrationTestRowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    // === 테스트 정보(대부분 null 허용) ===
    @Column(name = "test_id", length = 50, nullable = true)
    private String testId; // 테스트ID

    @Column(name = "module_name", length = 200, nullable = true)
    private String moduleName; // 모듈명

    @Column(name = "scenario", columnDefinition = "TEXT", nullable = true)
    private String scenario; // 시나리오

    @Column(name = "expected", columnDefinition = "TEXT", nullable = true)
    private String expected; // 기대 결과

    @Column(name = "result", columnDefinition = "TEXT", nullable = true)
    private String result; // 실제 결과(성공/실패 + 상세)

    @Column(name = "note", columnDefinition = "TEXT", nullable = true)
    private String note; // 비고(버그 ID 등)

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