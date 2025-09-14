// backEnd/together/src/main/java/com/together/report/ReportEntity.java
package com.together.report;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * 보고서
 */
@Entity
@Table(name = "report_entity")
@Getter
@Setter
public class ReportEntity {

    // 보고서 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 보고서 제목 (길이 제한 없음)
    @Column(columnDefinition = "TEXT", nullable = true)
    private String title;

    // 보고서 기간
    @Column(nullable = true)
    private String period;

    // 카테고리 (기획, 설계, 개발, 테스트)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    // 금주 진행 내용 (길이 제한 없음)
    @Column(columnDefinition = "TEXT", nullable = true)
    private String weeklyProgress;

    // 문제점 및 해결방안 (길이 제한 없음)
    @Column(columnDefinition = "TEXT", nullable = true)
    private String problemsAndSolutions;

    // 향후 계획 (길이 제한 없음)
    @Column(columnDefinition = "TEXT", nullable = true)
    private String futurePlans;

    // 보고서를 작성한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    // 보고서가 속한 프로젝트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    // 생성 시간
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 수정 시간
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}