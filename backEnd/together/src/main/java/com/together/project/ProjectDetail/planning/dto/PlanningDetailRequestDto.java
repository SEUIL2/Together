package com.together.project.ProjectDetail.planning.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PlanningDetailRequestDto {
    private String type;                  // 예: motivation, goal, description...
    private String text;                  // 텍스트 내용
    private List<MultipartFile> files;    // 업로드할 파일들
}