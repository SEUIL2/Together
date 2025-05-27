package com.together.user.professor.projectmemo;

import com.together.project.ProjectEntity;
import com.together.user.professor.ProfessorEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * 교수 전용 프로젝트 메모 엔티티
 * - 프로젝트당 여러 개의 메모를 남길 수 있음
 * - 교수와 프로젝트를 ManyToOne으로 연결
 */
@Entity
@Table(name = "professor_project_memo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessorProjectMemoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 메모 작성자 (교수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private ProfessorEntity professor;

    // 🔗 메모가 속한 프로젝트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    // 📄 메모 내용 (TEXT 타입)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 🕒 메모 최초 작성 시간
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    // 🕓 메모 마지막 수정 시간
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // ✅ 생성 시점 자동 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // ✅ 수정 시점 자동 갱신
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}