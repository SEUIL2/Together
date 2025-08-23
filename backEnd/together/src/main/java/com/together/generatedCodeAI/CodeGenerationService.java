package com.together.generatedCodeAI;

import com.together.generatedCodeAI.AI.CustomOpenAiService;
import com.together.generatedCodeAI.DTO.ClassDiagramCodeRequest;
import com.together.generatedCodeAI.DTO.ErdCodeRequest;
import com.together.generatedCodeAI.DTO.GeneratedCodeResponse;
import com.together.ProjectDetail.design.DesignDetailRepository;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CodeGenerationService {

    private final GeneratedCodeRepository codeRepo;
    private final DesignDetailRepository detailRepo;
    private final CustomOpenAiService aiService;
    private final ProjectRepository projectRepo;

    /**
     * 클래스 다이어그램 기반으로 코드 생성
     */
    public Long generateFromClass(ClassDiagramCodeRequest req, UserEntity user, Long projectId) {
        ProjectEntity project = projectRepo.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));

        // 1. 프로젝트에 저장된 클래스 다이어그램 조회
        String diagram = getClassDiagramJson(project);

        // 2. AI 서비스로부터 코드 생성 요청
        String generated = aiService.generateFromClassDiagram(diagram, req.getLanguage());

        // 3. 생성된 코드 저장 및 ID 반환
        return save(req.getCodeName(), req.getLanguage(), CodeSourceType.CLASS_DIAGRAM, user, project, generated);
    }

    /**
     * ER 다이어그램 기반으로 코드 생성
     */
    public Long generateFromErd(ErdCodeRequest req, UserEntity user, Long projectId) {
        ProjectEntity project = projectRepo.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));

        String erd = getErdText(project);
        String generated = aiService.generateFromErd(erd, req.getLanguage());

        return save(req.getCodeName(), req.getLanguage(), CodeSourceType.ER_DIAGRAM, user, project, generated);
    }

    // 클래스 다이어그램 JSON 조회
    private String getClassDiagramJson(ProjectEntity project) {
        return detailRepo.findByProject(project).orElseThrow().getClassDiagramJson();
    }

    // ERD Text 조회
    private String getErdText(ProjectEntity project) {
        return detailRepo.findByProject(project).orElseThrow().getErdJson();
    }

    // 코드 저장 처리 (공통 로직)
    private Long save(String name, String lang, CodeSourceType type, UserEntity user, ProjectEntity project, String code) {
        return codeRepo.save(GeneratedCodeEntity.builder()
                .codeName(name)
                .language(lang)
                .type(type)
                .author(user)
                .project(project)
                .generatedCode(code)
                .createdAt(LocalDateTime.now())
                .build()).getId();
    }

    /**
     * 프로젝트에 속한 전체 생성 코드 조회
     */
    public List<GeneratedCodeResponse> getAll(Long projectId) {
        ProjectEntity project = projectRepo.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));

        return codeRepo.findByProject(project).stream()
                .map(this::toDto).toList();
    }

    /**
     * 특정 생성 코드 단건 조회
     */
    public GeneratedCodeResponse getOne(Long id) {
        return codeRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("코드 없음"));
    }

    /**
     * 코드 삭제 (작성자 본인만 가능)
     */
    public void delete(Long id, UserEntity user) {
        GeneratedCodeEntity entity = codeRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("코드 없음"));
        if (!entity.getAuthor().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("삭제 권한 없음");
        }
        codeRepo.deleteById(id);
    }

    // Entity → DTO 변환
    private GeneratedCodeResponse toDto(GeneratedCodeEntity e) {
        return GeneratedCodeResponse.builder()
                .id(e.getId())
                .codeName(e.getCodeName())
                .language(e.getLanguage())
                .type(e.getType())
                .authorName(e.getAuthor().getUserName())
                .generatedCode(e.getGeneratedCode())
                .createdAt(e.getCreatedAt())
                .build();
    }


}
