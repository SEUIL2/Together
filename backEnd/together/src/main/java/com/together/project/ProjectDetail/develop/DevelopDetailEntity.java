package com.together.project.ProjectDetail.develop;


import com.together.project.ProjectDetail.common.FileMeta;
import com.together.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class DevelopDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private ProjectEntity project;

    @Column(columnDefinition = "TEXT")
    private String environmentText;

    @Column(columnDefinition = "TEXT")
    private String versioningText;

    @Column(columnDefinition = "TEXT")
    private String commitRuleText;

    @Column(columnDefinition = "TEXT")
    private String folderText;

    @Column(columnDefinition = "TEXT")
    private String codingStandardText;

    @Column(columnDefinition = "TEXT")
    private String unitTestText;

    @Column(columnDefinition = "TEXT")
    private String integrationTestText;

    private LocalDateTime updatedAt;

    @ElementCollection
    private List<FileMeta> environmentFiles = new ArrayList<>();

    @ElementCollection
    private List<FileMeta> versioningFiles = new ArrayList<>();

    @ElementCollection
    private List<FileMeta> commitRuleFiles = new ArrayList<>();

    @ElementCollection
    private List<FileMeta> folderFiles = new ArrayList<>();

    @ElementCollection
    private List<FileMeta> codingStandardFiles = new ArrayList<>();

    @ElementCollection
    private List<FileMeta> unitTestFiles = new ArrayList<>();

    @ElementCollection
    private List<FileMeta> integrationTestFiles = new ArrayList<>();
}