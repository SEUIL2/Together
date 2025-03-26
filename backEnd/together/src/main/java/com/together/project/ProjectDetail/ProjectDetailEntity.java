package com.together.project.ProjectDetail;



import com.together.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project_detail")
public class ProjectDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column
    private String projectMotivation;   // 프로젝트 동기 (텍스트 저장)

    @Column
    private String projectGoal;         // 프로젝트 목표 (텍스트 저장)

    @Column
    private String requirementsImage;   // 요구사항 정의 (Google Drive URL)

    @Column
    private String infoStructure;       // 정보 구조도 (Google Drive URL)

    @Column
    private String storyboard;          // 스토리보드 (텍스트)

    @Column
    private String useCaseDiagramImage; // 유스케이스 다이어그램 (Google Drive URL)

    @Column
    private String classDiagramImage;   // 클래스 다이어그램 (Google Drive URL)

    @Column
    private String sequenceDiagramImage; // 시퀀스 다이어그램 (Google Drive URL)

    @Column
    private String uiDesign;            // UI 디자인 (텍스트)

    @Column
    private String erDiagramImage;      // ER 다이어그램 (Google Drive URL)

    @Column
    private String tableSpecImage;// 테이블 명세서 (Google Drive URL)

   @Column
    private String systemArchitecture; //시스템 아키텍쳐

    @Column
    private String devEnvironmentText;         // 개발 환경 설정 (텍스트)

    @Column
    private String versionControlStrategy;     // 버전 관리 전략 수립 (텍스트)

    @Column
    private String commitMessageRule;          // 커밋 메시지 규칙 정하기 (텍스트)

    @Column
    private String folderNamingRule;           // 폴더 구조 및 파일 네이밍 규칙 (텍스트)

    @Column
    private String schedulePlanImage;   // 프로젝트 일정계획 (Google Drive URL)

    @Column
    private String devEnvironmentImage; // 개발환경 설정 (Google Drive URL)

    @Column
    private String codingStandardImage; // 코딩 표준 지정 (Google Drive URL)

    @Column
    private String unitTestImage;       // 단위 테스트 (Google Drive URL)

    @Column
    private String integrationTestImage; // 통합 테스트 (Google Drive URL)



    // ✅ 새로운 생성자 추가
    public ProjectDetailEntity(ProjectEntity project, String projectMotivation, String projectGoal,
                               String storyboard, String uiDesign, String infoStructure) {
        this.project = project;
        this.projectMotivation = projectMotivation;
        this.projectGoal = projectGoal;
        this.storyboard = storyboard;
        this.uiDesign = uiDesign;
        this.infoStructure = infoStructure;
    }
}

