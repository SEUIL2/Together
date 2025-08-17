package com.together.ProjectDetail.develop.dto;

import com.together.ProjectDetail.common.FileMetaDto;
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
