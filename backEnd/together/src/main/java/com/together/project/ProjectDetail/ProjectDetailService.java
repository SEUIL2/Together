package com.together.project.ProjectDetail;

import com.together.documentManger.GoogleDriveService;
import com.together.project.ProjectDetail.dto.ProjectDetailResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailSimpleResponseDto;
import com.together.project.ProjectDetail.dto.ProjectDetailTextResponseDto;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            String folderNamingRule,
            String projectDescription
    ) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        ProjectDetailEntity detail = projectDetailRepository.findByProject(project)
                .orElseGet(() -> {
                    ProjectDetailEntity newDetail = new ProjectDetailEntity();
                    newDetail.setProject(project);
                    return projectDetailRepository.save(newDetail);
                });

        // ✅ null이 아닌 필드만 업데이트
        if (projectMotivation != null) detail.setProjectMotivation(projectMotivation);
        if (projectGoal != null) detail.setProjectGoal(projectGoal);
        if (storyboard != null) detail.setStoryboard(storyboard);
        if (uiDesign != null) detail.setUiDesign(uiDesign);
        if (systemArchitecture != null) detail.setSystemArchitecture(systemArchitecture);
        if (devEnvironmentText != null) detail.setDevEnvironmentText(devEnvironmentText);
        if (versionControlStrategy != null) detail.setVersionControlStrategy(versionControlStrategy);
        if (commitMessageRule != null) detail.setCommitMessageRule(commitMessageRule);
        if (folderNamingRule != null) detail.setFolderNamingRule(folderNamingRule);
        if (projectDescription != null) detail.setProjectDescription(projectDescription);

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
                detail.getFolderNamingRule(),
                detail.getProjectDescription()
        );
    }

    // ✅ DTO 매핑 로직
    public ProjectDetailResponseDto getProjectDetail(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        // ✅ 상세 정보 없으면 새로 생성
        ProjectDetailEntity detail = projectDetailRepository.findByProject(project)
                .orElseGet(() -> {
                    ProjectDetailEntity newDetail = new ProjectDetailEntity();
                    newDetail.setProject(project);
                    return projectDetailRepository.save(newDetail);
                });

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
                detail.getIntegrationTestImage(),
                detail.getProjectDescription()
        );
    }

    //전체 정보 수정
    @Transactional
    public ProjectDetailResponseDto updateFullDetails(
            Long projectId,
            String projectMotivation,
            String projectGoal,
            String storyboard,
            String uiDesign,
            String systemArchitecture,
            String devEnvironmentText,
            String versionControlStrategy,
            String commitMessageRule,
            String folderNamingRule,
            String projectDescription,
            MultipartFile requirementsImage,
            MultipartFile infoStructure,
            MultipartFile useCaseDiagramImage,
            MultipartFile classDiagramImage,
            MultipartFile sequenceDiagramImage,
            MultipartFile erDiagramImage,
            MultipartFile tableSpecImage,
            MultipartFile schedulePlanImage,
            MultipartFile devEnvironmentImage,
            MultipartFile codingStandardImage,
            MultipartFile unitTestImage,
            MultipartFile integrationTestImage
    ) throws IOException {

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다."));

        ProjectDetailEntity detail = projectDetailRepository.findByProject(project)
                .orElseGet(() -> new ProjectDetailEntity(project, null, null, null, null, null));

        // ✅ 텍스트 필드 - null 체크 후 업데이트
        if (projectMotivation != null) detail.setProjectMotivation(projectMotivation);
        if (projectGoal != null) detail.setProjectGoal(projectGoal);
        if (storyboard != null) detail.setStoryboard(storyboard);
        if (uiDesign != null) detail.setUiDesign(uiDesign);
        if (systemArchitecture != null) detail.setSystemArchitecture(systemArchitecture);
        if (devEnvironmentText != null) detail.setDevEnvironmentText(devEnvironmentText);
        if (versionControlStrategy != null) detail.setVersionControlStrategy(versionControlStrategy);
        if (commitMessageRule != null) detail.setCommitMessageRule(commitMessageRule);
        if (folderNamingRule != null) detail.setFolderNamingRule(folderNamingRule);
        if (projectDescription != null) detail.setProjectDescription(projectDescription);

        // ✅ 이미지 필드 - null & isEmpty 체크 후 업로드 및 반영
        if (requirementsImage != null && !requirementsImage.isEmpty()) {
            detail.setRequirementsImage(googleDriveService.uploadImageToGoogleDrive(requirementsImage));
        }
        if (infoStructure != null && !infoStructure.isEmpty()) {
            detail.setInfoStructure(googleDriveService.uploadImageToGoogleDrive(infoStructure));
        }
        if (useCaseDiagramImage != null && !useCaseDiagramImage.isEmpty()) {
            detail.setUseCaseDiagramImage(googleDriveService.uploadImageToGoogleDrive(useCaseDiagramImage));
        }
        if (classDiagramImage != null && !classDiagramImage.isEmpty()) {
            detail.setClassDiagramImage(googleDriveService.uploadImageToGoogleDrive(classDiagramImage));
        }
        if (sequenceDiagramImage != null && !sequenceDiagramImage.isEmpty()) {
            detail.setSequenceDiagramImage(googleDriveService.uploadImageToGoogleDrive(sequenceDiagramImage));
        }
        if (erDiagramImage != null && !erDiagramImage.isEmpty()) {
            detail.setErDiagramImage(googleDriveService.uploadImageToGoogleDrive(erDiagramImage));
        }
        if (tableSpecImage != null && !tableSpecImage.isEmpty()) {
            detail.setTableSpecImage(googleDriveService.uploadImageToGoogleDrive(tableSpecImage));
        }
        if (schedulePlanImage != null && !schedulePlanImage.isEmpty()) {
            detail.setSchedulePlanImage(googleDriveService.uploadImageToGoogleDrive(schedulePlanImage));
        }
        if (devEnvironmentImage != null && !devEnvironmentImage.isEmpty()) {
            detail.setDevEnvironmentImage(googleDriveService.uploadImageToGoogleDrive(devEnvironmentImage));
        }
        if (codingStandardImage != null && !codingStandardImage.isEmpty()) {
            detail.setCodingStandardImage(googleDriveService.uploadImageToGoogleDrive(codingStandardImage));
        }
        if (unitTestImage != null && !unitTestImage.isEmpty()) {
            detail.setUnitTestImage(googleDriveService.uploadImageToGoogleDrive(unitTestImage));
        }
        if (integrationTestImage != null && !integrationTestImage.isEmpty()) {
            detail.setIntegrationTestImage(googleDriveService.uploadImageToGoogleDrive(integrationTestImage));
        }

        // ✅ 저장
        projectDetailRepository.save(detail);

        // ✅ 응답 DTO 반환
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
                detail.getIntegrationTestImage(),
                detail.getProjectDescription()
        );
    }

}