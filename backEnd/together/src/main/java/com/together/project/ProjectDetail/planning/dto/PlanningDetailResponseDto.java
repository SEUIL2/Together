package com.together.project.ProjectDetail.planning.dto;

import com.together.project.ProjectDetail.common.FileMetaDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PlanningDetailResponseDto {
    private String type;            // 항목 종류
    private String text;            // 저장된 텍스트
    private List<FileMetaDto> files; // 저장된 파일 목록
}