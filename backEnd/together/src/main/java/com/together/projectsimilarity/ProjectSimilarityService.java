package com.together.projectsimilarity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.together.ProjectDetail.design.DesignDetailEntity;
import com.together.ProjectDetail.design.DesignDetailRepository;
import com.together.ProjectDetail.develop.DevelopmentEnvironmentEntity;
import com.together.ProjectDetail.develop.DevelopmentEnvironmentRepository;
import com.together.ProjectDetail.planning.PlanningDetailEntity;
import com.together.ProjectDetail.planning.PlanningDetailRepository;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.projectsimilarity.dto.ProjectComparisonRequestDto;
import com.together.projectsimilarity.dto.SimilarityReportDto;
import com.together.projectsimilarity.dto.SingleComparisonResultDto;
import com.together.topicRecommendAI.service.OpenAiService;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectSimilarityService {

    private final ProjectRepository projectRepository;
    private final PlanningDetailRepository planningDetailRepository;
    private final DesignDetailRepository designDetailRepository;
    private final DevelopmentEnvironmentRepository devEnvRepository;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    /**
     * 요청된 프로젝트들 간의 유사도를 비교하여 리포트를 생성합니다.
     * @param requestDto 기준 프로젝트와 비교 대상 프로젝트 정보
     * @return 생성된 유사도 리포트
     */
    @Transactional(readOnly = true)
    public SimilarityReportDto compareProjects(ProjectComparisonRequestDto requestDto) {
        String baseProjectData = getProjectDataAsString(requestDto.getBaseProjectId());

        List<SingleComparisonResultDto> results = new ArrayList<>();
        for (Long targetId : requestDto.getTargetProjectIds()) {
            try {
                String targetProjectData = getProjectDataAsString(targetId);
                String prompt = buildComparisonPrompt(baseProjectData, targetProjectData);

                // OpenAI 서비스를 호출하여 JSON 형태의 응답을 받습니다.
                String aiResponseJson = openAiService.getChatResponse(prompt); //topicRecommendAI의 OpenAiService 페이지 빌렸음

                // AI가 응답을 Markdown 코드 블록으로 감싸는 경우를 대비하여 정리합니다.
                aiResponseJson = aiResponseJson.replace("```json", "").replace("```", "").trim();

                SingleComparisonResultDto resultDto = objectMapper.readValue(aiResponseJson, SingleComparisonResultDto.class);

                // 최종 결과 객체를 생성합니다. (AI 응답에 targetProjectId가 없으므로 직접 설정)
                results.add(new SingleComparisonResultDto(
                        targetId,
                        resultDto.getTopicSimilarity(),
                        resultDto.getFunctionSimilarity(),
                        resultDto.getTechStackSimilarity(),
                        resultDto.getKeywordSimilarity()
                ));
            } catch (Exception e) {
                log.error("Failed to compare project {} with project {}", requestDto.getBaseProjectId(), targetId, e);
            }
        }

        return new SimilarityReportDto(requestDto.getBaseProjectId(), results);
    }

    /**
     * 프로젝트 ID를 기반으로 DB에서 관련 데이터를 모두 조회하여 하나의 문자열로 합칩니다.
     * @param projectId 조회할 프로젝트의 ID
     * @return AI에게 전달할 포맷의 프로젝트 데이터 문자열
     */
    private String getProjectDataAsString(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        StringBuilder sb = new StringBuilder();
        sb.append("## Project: ").append(project.getTitle()).append(" (ID: ").append(projectId).append(")\n\n");

        // 1. 기획 (Planning) - 올바른 필드명(getter)으로 수정
        sb.append("### 1. Planning Phase\n");
        planningDetailRepository.findByProject(project).ifPresent(p -> {
            sb.append("- Motivation: ").append(p.getMotivationText()).append("\n");
            sb.append("- Goal: ").append(p.getGoalText()).append("\n");
            sb.append("- Requirements: ").append(p.getRequirementText()).append("\n");
            sb.append("- Info Structure (Text): ").append(p.getInfoStructureText()).append("\n");
            sb.append("- Info Structure (JSON): ").append(p.getInfoStructureJson()).append("\n");
            sb.append("- Storyboard: ").append(p.getStoryboardText()).append("\n");
        });
        sb.append("\n");

        // 2. 설계 (Design) - 올바른 필드명(getter)으로 수정
        sb.append("### 2. Design Phase\n");
        designDetailRepository.findByProject(project).ifPresent(d -> {
            sb.append("- Use Case Diagram (Text): ").append(d.getUsecaseText()).append("\n");
            sb.append("- Use Case Diagram (JSON): ").append(d.getUsecaseJson()).append("\n");
            sb.append("- Class Diagram (Text): ").append(d.getClassDiagramText()).append("\n");
            sb.append("- Class Diagram (JSON): ").append(d.getClassDiagramJson()).append("\n");
            sb.append("- Sequence Diagram (Text): ").append(d.getSequenceText()).append("\n");
            sb.append("- Sequence Diagram (JSON): ").append(d.getSequenceJson()).append("\n");
            sb.append("- ERD (Text): ").append(d.getErdText()).append("\n");
            sb.append("- ERD (JSON): ").append(d.getErdJson()).append("\n");
            sb.append("- Table Specification: ").append(d.getTableSpecText()).append("\n");
            sb.append("- System Architecture: ").append(d.getArchitectureText()).append("\n");
        });
        sb.append("\n");

        // 3. 개발 (Development) - 올바른 필드명(getter)으로 수정
        sb.append("### 3. Development Phase (Tech Stack)\n");
        List<DevelopmentEnvironmentEntity> devEnvs = devEnvRepository.findAllByProjectEntity_ProjectId(project.getProjectId());
        if (!devEnvs.isEmpty()) {
            devEnvs.forEach(env -> {
                sb.append("  - OS: ").append(env.getOperatingSystem()).append("\n");
                sb.append("  - IDE: ").append(env.getIde()).append("\n");
                sb.append("  - Language: ").append(env.getDevLanguage()).append("\n");
                sb.append("  - Framework: ").append(env.getFramework()).append("\n");
                sb.append("  - Version Control: ").append(env.getVersionControl()).append("\n");
                sb.append("  - DBMS: ").append(env.getDatabase()).append("\n");
                sb.append("  - ETC: ").append(env.getEtc()).append("\n");
            });
        }
        sb.append("\n");

        return sb.toString();
    }


    /**
     * 두 프로젝트 데이터를 기반으로 OpenAI에 전송할 프롬프트를 생성합니다.
     * @param projectAData 첫 번째 프로젝트 데이터
     * @param projectBData 두 번째 프로젝트 데이터
     * @return 완성된 프롬프트 문자열
     */
    private String buildComparisonPrompt(String projectAData, String projectBData) {
        return "You are an expert AI professor specializing in software engineering project evaluation. Your task is to analyze and compare two student projects based on the provided data. Provide a similarity analysis for four specific categories. The output MUST be a single, well-formed JSON object without any additional text, explanations, or markdown formatting. **Crucially, the 'reason' field within the JSON response MUST be written in Korean.** The JSON must conform to the following structure:\n\n" +
                "{\n" +
                "  \"topicSimilarity\": {\"score\": <integer, 0-100>, \"reason\": \"<string, must be in Korean>\"},\n" +
                "  \"functionSimilarity\": {\"score\": <integer, 0-100>, \"reason\": \"<string, must be in Korean>\"},\n" +
                "  \"techStackSimilarity\": {\"score\": <integer, 0-100>, \"reason\": \"<string, must be in Korean>\"},\n" +
                "  \"keywordSimilarity\": {\"score\": <integer, 0-100>, \"reason\": \"<string, must be in Korean>\"}\n" +
                "}\n\n" +
                "Here is the data for the two projects:\n\n" +
                "--- PROJECT A ---\n" +
                projectAData +
                "\n\n--- PROJECT B ---\n" +
                projectBData +
                "\n\n--- ANALYSIS START ---\n" +
                "Now, provide the JSON output for the comparison between Project A and Project B, ensuring all 'reason' fields are in Korean.";
    }
}