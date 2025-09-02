package com.together.ProjectDetail.planning;

import com.together.project.ProjectEntity;
import com.together.ProjectDetail.common.FileMeta;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "planning_detail")
public class PlanningDetailEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    private LocalDateTime updatedAt;

    // ✏️ 프로젝트 동기
    @Column(columnDefinition = "TEXT")
    private String motivationText;
    @ElementCollection
    @CollectionTable(name = "planning_motivation_files", joinColumns = @JoinColumn(name = "planning_id"))
    private List<FileMeta> motivationFiles = new ArrayList<>();

    // ✏️ 프로젝트 목표
    @Column(columnDefinition = "TEXT")
    private String goalText;
    @ElementCollection
    @CollectionTable(name = "planning_goal_files", joinColumns = @JoinColumn(name = "planning_id"))
    private List<FileMeta> goalFiles = new ArrayList<>();

    // ✏️ 프로젝트 설명
    @Column(columnDefinition = "TEXT")
    private String descriptionText;
    @ElementCollection
    @CollectionTable(name = "planning_description_files", joinColumns = @JoinColumn(name = "planning_id"))
    private List<FileMeta> descriptionFiles = new ArrayList<>();

    // ✏️ 요구사항 정의
    @Column(columnDefinition = "TEXT")
    private String requirementText;
    @ElementCollection
    @CollectionTable(name = "planning_requirements_files", joinColumns = @JoinColumn(name = "planning_id"))
    private List<FileMeta> requirementFiles = new ArrayList<>();

    // ✏️ 정보 구조도
    @Column(columnDefinition = "TEXT")
    private String infoStructureText;
    @ElementCollection
    @CollectionTable(name = "planning_info_structure_files", joinColumns = @JoinColumn(name = "planning_id"))
    private List<FileMeta> infoStructureFiles = new ArrayList<>();

    //✏️ 정보구조도 JSON
    @Column(columnDefinition = "TEXT")
    private String infoStructureJson; // ⭐️ 반드시 여기에 추가


    // ✏️ 스토리보드
    @Column(columnDefinition = "TEXT")
    private String storyboardText;
    @ElementCollection
    @CollectionTable(name = "planning_storyboard_files", joinColumns = @JoinColumn(name = "planning_id"))
    private List<FileMeta> storyboardFiles = new ArrayList<>();
}
