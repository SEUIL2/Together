package com.together.project.ProjectDetail.design;

import com.together.project.ProjectDetail.common.FileMeta;
import com.together.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 프로젝트 연관
    @OneToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    // 📌 각 항목별 텍스트 필드
    @Column(columnDefinition = "TEXT")
    private String usecaseText;

    @Column(columnDefinition = "TEXT")
    private String classDiagramText;

    @Column(columnDefinition = "TEXT")
    private String classDiagramJson; // <-- 여기 추가!

    @Column(columnDefinition = "TEXT")
    private String sequenceText;

    @Column(columnDefinition = "TEXT")
    private String uiDesignText;

    @Column(columnDefinition = "TEXT")
    private String erdText;

    @Column(columnDefinition = "TEXT")
    private String tableSpecText;

    @Column(columnDefinition = "TEXT")
    private String architectureText;

    @Column(columnDefinition = "TEXT")
    private String scheduleText;

    // 📎 각 항목별 파일 리스트
    @ElementCollection
    @CollectionTable(name = "design_usecase_files", joinColumns = @JoinColumn(name = "design_id"))
    private List<FileMeta> usecaseFiles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "design_class_diagram_files", joinColumns = @JoinColumn(name = "design_id"))
    private List<FileMeta> classDiagramFiles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "design_sequence_files", joinColumns = @JoinColumn(name = "design_id"))
    private List<FileMeta> sequenceFiles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "design_ui_files", joinColumns = @JoinColumn(name = "design_id"))
    private List<FileMeta> uiDesignFiles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "design_erd_files", joinColumns = @JoinColumn(name = "design_id"))
    private List<FileMeta> erdFiles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "design_table_spec_files", joinColumns = @JoinColumn(name = "design_id"))
    private List<FileMeta> tableSpecFiles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "design_architecture_files", joinColumns = @JoinColumn(name = "design_id"))
    private List<FileMeta> architectureFiles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "design_schedule_files", joinColumns = @JoinColumn(name = "design_id"))
    private List<FileMeta> scheduleFiles = new ArrayList<>();

    private LocalDateTime updatedAt;
}
