package com.together.TeamMemberManagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "evaluation_entity")
public class EvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long evaluationId;

    @ManyToOne
    @JoinColumn(name = "evaluator_id", referencedColumnName = "user_id")  // ✅ FK 설정// ✅ 평가자 참조
    private UserEntity evaluator;

    @ManyToOne
    @JoinColumn(name = "evaluatee_id", referencedColumnName = "user_id")  // ✅ FK 설정 ✅ 평가 대상 참조
    private UserEntity evaluatee;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")  // ✅ FK 설정 ✅ 프로젝트
    private ProjectEntity project;

    @Column(nullable = false)
    private int score;

    @Column(length = 500)
    private String comment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime evaluationDate;
}