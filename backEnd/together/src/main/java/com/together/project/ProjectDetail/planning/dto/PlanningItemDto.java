package com.together.project.ProjectDetail.planning.dto;

import com.together.project.ProjectDetail.common.FileMetaDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 하나의 기획 항목을 구성하는 DTO (텍스트 + 파일 목록)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningItemDto {
    private String text; // 텍스트 내용
    private List<FileMetaDto> files; // 업로드된 파일 리스트 (url + 업로드 시간)
}
