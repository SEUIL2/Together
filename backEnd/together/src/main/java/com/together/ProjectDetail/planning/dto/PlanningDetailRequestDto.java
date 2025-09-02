package com.together.ProjectDetail.planning.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PlanningDetailRequestDto {
    private String type;                  // 예: motivation, goal, description...
    private String text;                  // 텍스트 내용
    private String json;                    // ⭐️ JSON 데이터 저장용 필드 (정보구조도 등, 필요시만 사용)
    private List<MultipartFile> files;    // 업로드할 파일들
}