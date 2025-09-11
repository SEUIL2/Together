package com.together.ProjectDetail.develop;

import com.together.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/**
 * 기능별 개발 순서 항목
 */
@Entity
@Table(name = "dev_order_item")
@Getter
@Setter
public class DevOrderItemEntity {

    // 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 기능 이름 (길이 제한 없음)
     */
    @Lob // <-- 이 줄을 추가했습니다!
    @Column(nullable = true)
    private String featureName;

    // 기능 순서
    @Column(nullable = true)
    private Integer featureOrder;

    // 중요도
    @Column(nullable = true)
    private String priority;

    /**
     * 기능 설명 (길이 제한 없음)
     */
    @Lob
    @Column(nullable = true)
    private String description;

    // 완료 여부 (기본값 false)
    @Column(nullable = false)
    @ColumnDefault("0")
    private boolean completed = false;

    // 소속 프로젝트 (필수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;
}