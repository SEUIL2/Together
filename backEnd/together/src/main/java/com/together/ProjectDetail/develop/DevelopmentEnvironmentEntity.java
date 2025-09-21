package com.together.ProjectDetail.develop;

import com.together.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "development_environment_entity")
@NoArgsConstructor
public class DevelopmentEnvironmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String operatingSystem; // 운영체제

    @Column(columnDefinition = "TEXT")
    private String ide; // IDE

    @Column(columnDefinition = "TEXT")
    private String devLanguage; // 개발 언어

    @Column(columnDefinition = "TEXT")
    private String framework; // 프레임워크

    @Column(columnDefinition = "TEXT")
    private String versionControl; // 버전관리 시스템

    @Column(name = "database_system", columnDefinition = "TEXT")
    private String database; // 데이터베이스

    @Column(columnDefinition = "TEXT")
    private String etc; // 기타

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity projectEntity;
}