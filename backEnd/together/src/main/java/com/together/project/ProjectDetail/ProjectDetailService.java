package com.together.project.ProjectDetail;

import com.together.documentManger.GoogleDriveService;
import com.together.project.ProjectDetail.dto.ProjectDetailResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailSimpleResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailTextResponseDto;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProjectDetailService {

    private final ProjectDetailRepository projectDetailRepository;
    private final ProjectRepository projectRepository;
    private final GoogleDriveService googleDriveService;


    // ✅ 프로젝트 상세 정보 조회 (DTO 반환)
    /*public ProjectDetailResponseDto getProjectDetail(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        ProjectDetailEntity detail = projectDetailRepository.findByProject(project)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트의 상세 정보를 찾을 수 없습니다."));


    }*/
        // ✅ 프로젝트 이미지 업로드 (Google Drive)
    @Transactional
    public ProjectDetailSimpleResponseDto uploadProjectImage(Long projectId, MultipartFile file, String imageType) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 선택해주세요.");
        }

        if (imageType == null || imageType.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 유형 (imageType)을 입력해주세요.");
        }

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        String uploadedImageUrl = googleDriveService.uploadImageToGoogleDrive(file);

        ProjectDetailEntity projectDetail = projectDetailRepository.findByProject(project)
                .orElseGet(() -> new ProjectDetailEntity(project, null, null, null, null, null));

        switch (imageType.toLowerCase()) {
            case "requirements":
                projectDetail.setRequirementsImage(uploadedImageUrl);
                break;
            case "infostructure":
                projectDetail.setInfoStructure(uploadedImageUrl);
                break;
            case "usecasediagram":
                projectDetail.setUseCaseDiagramImage(uploadedImageUrl);
                break;
            case "classdiagram":
                projectDetail.setClassDiagramImage(uploadedImageUrl);
                break;
            case "sequencediagram":
                projectDetail.setSequenceDiagramImage(uploadedImageUrl);
                break;
            case "erdiagram":
                projectDetail.setErDiagramImage(uploadedImageUrl);
                break;
            case "tablespec":
                projectDetail.setTableSpecImage(uploadedImageUrl);
                break;
            case "scheduleplan":
                projectDetail.setSchedulePlanImage(uploadedImageUrl);
                break;
            case "devenvironment":
                projectDetail.setDevEnvironmentImage(uploadedImageUrl);
                break;
            case "codingstandard":
                projectDetail.setCodingStandardImage(uploadedImageUrl);
                break;
            case "unittest":
                projectDetail.setUnitTestImage(uploadedImageUrl);
                break;
            case "integrationtest":
                projectDetail.setIntegrationTestImage(uploadedImageUrl);
                break;
            default:
                throw new IllegalArgumentException("올바르지 않은 이미지 유형입니다.");
        }

        projectDetailRepository.save(projectDetail);

        return new ProjectDetailSimpleResponseDto("이미지가 성공적으로 업로드되었습니다.", uploadedImageUrl);
    }

    // ✅ 프로젝트 텍스트 저장
    @Transactional
    public ProjectDetailTextResponseDto updateTextDetails(
            Long projectId,
            String projectMotivation,
            String projectGoal,
            String storyboard,
            String uiDesign,
            String systemArchitecture,
            String devEnvironmentText,
            String versionControlStrategy,
            String commitMessageRule,
            String folderNamingRule
    ) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        ProjectDetailEntity detail = projectDetailRepository.findByProject(project)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트의 상세 정보를 찾을 수 없습니다."));

        detail.setProjectMotivation(projectMotivation);
        detail.setProjectGoal(projectGoal);
        detail.setStoryboard(storyboard);
        detail.setUiDesign(uiDesign);
        detail.setSystemArchitecture(systemArchitecture);
        detail.setDevEnvironmentText(devEnvironmentText);
        detail.setVersionControlStrategy(versionControlStrategy);
        detail.setCommitMessageRule(commitMessageRule);
        detail.setFolderNamingRule(folderNamingRule);

        projectDetailRepository.save(detail);

        return new ProjectDetailTextResponseDto(
                detail.getProjectMotivation(),
                detail.getProjectGoal(),
                detail.getStoryboard(),
                detail.getUiDesign(),
                detail.getSystemArchitecture(),
                detail.getDevEnvironmentText(),
                detail.getVersionControlStrategy(),
                detail.getCommitMessageRule(),
                detail.getFolderNamingRule()
        );
    }

    // ✅ DTO 매핑 로직
    public ProjectDetailResponseDto getProjectDetail(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        ProjectDetailEntity detail = projectDetailRepository.findByProject(project)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트의 상세 정보를 찾을 수 없습니다."));

        return new ProjectDetailResponseDto(
                detail.getProjectMotivation(),
                detail.getProjectGoal(),
                detail.getRequirementsImage(),
                detail.getInfoStructure(),
                detail.getStoryboard(),
                detail.getUseCaseDiagramImage(),
                detail.getClassDiagramImage(),
                detail.getSequenceDiagramImage(),
                detail.getUiDesign(),
                detail.getErDiagramImage(),
                detail.getTableSpecImage(),
                detail.getSchedulePlanImage(),
                detail.getDevEnvironmentImage(),
                detail.getCodingStandardImage(),
                detail.getUnitTestImage(),
                detail.getIntegrationTestImage()
        );
    }
}