package com.together.projectsimilarity;

import com.together.projectsimilarity.dto.ProjectComparisonRequestDto;
import com.together.projectsimilarity.dto.SimilarityReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/professor/project-similarity")
@RequiredArgsConstructor
public class ProjectSimilarityController {

    private final ProjectSimilarityService projectSimilarityService;

    /**
     * 프로젝트 유사도 비교를 요청하는 API
     * @param requestDto 기준 프로젝트 ID와 비교 대상 프로젝트 ID 리스트
     * @return 유사도 분석 리포트
     */
    @PostMapping("/compare")
    public ResponseEntity<SimilarityReportDto> compareProjects(@RequestBody ProjectComparisonRequestDto requestDto) {
        SimilarityReportDto report = projectSimilarityService.compareProjects(requestDto);
        return ResponseEntity.ok(report);
    }
}