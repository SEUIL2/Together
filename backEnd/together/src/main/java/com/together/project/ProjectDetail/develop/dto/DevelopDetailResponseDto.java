package com.together.project.ProjectDetail.develop.dto;

import com.together.project.ProjectDetail.common.FileMetaDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DevelopDetailResponseDto {
    private String type;
    private String text;
    private List<FileMetaDto> files;
}
