package com.together.worktask;

import com.together.comment.CommentEntity;
import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 작업(Task) 및 일정(Schedule)을 나타내는 엔티티.
 */
@Entity
@Table(name = "work_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkTaskEntity {

    /** 기본 키 (자동 생성, NOT NULL) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작업 제목 (TEXT 타입, NULL 허용) */
    @Column(name = "title", columnDefinition = "TEXT", nullable = true)
    private String title;

    /** 작업 상세 설명 (TEXT 타입, NULL 허용) */
    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    private String description;

    /** 작업 시작일 (NULL 허용) */
    @Column(name = "start_date", nullable = true)
    private LocalDate startDate;

    /** 작업 종료일 (NULL 허용) */
    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    /**
     * 담당자 (프로젝트 멤버, NULL 허용)
     * optional=true 로 명시
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_user_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity assignedUser;

    /**
     * 작업 상태 (PENDING, IN_PROGRESS, COMPLETED, NULL 허용)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true)
    private WorkTaskStatus status;

    /**
     * 소속 프로젝트 (NULL 허용)
     * @AuthenticationPrincipal에서 세팅
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "project_id", nullable = true)
    private ProjectEntity project;

    /** 상위 작업 (NULL 허용 → 최상위 작업) */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_task_id", nullable = true)
    private WorkTaskEntity parentTask;

    /** 하위 작업들 (NULL 허용) */
    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkTaskEntity> childTasks = new ArrayList<>();

    /**
     * 댓글들 (CommentType = TASK, NULL 허용)
     */
    @OneToMany
    @JoinColumn(name = "targetId", nullable = true)
    @Where(clause = "comment_type = 'TASK'")
    private List<CommentEntity> comments = new ArrayList<>();

    /** 생성일 (NULL 허용, 서비스 또는 JPA Auditing에서 채워주세요) */
    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;

    /** 수정일 (NULL 허용, 서비스 또는 JPA Auditing에서 채워주세요) */
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
}

