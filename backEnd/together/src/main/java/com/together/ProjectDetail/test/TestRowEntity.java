package com.together.ProjectDetail.test;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "test_row")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestRowEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project; // 소속 프로젝트

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestTableType tableType; // UNIT(단위)/INTEGRATION(통합) 중 하나

    @Column(nullable = false)
    private String itemName; // 테스트 항목명

    @Column(columnDefinition = "TEXT")
    private String description; // 설명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserEntity author; // 작성자

    @Column(nullable = false)
    private LocalDate createdDate; // 작성일

    @Column(nullable = false)
    private boolean completed = false; // 완료여부(체크박스)
}
