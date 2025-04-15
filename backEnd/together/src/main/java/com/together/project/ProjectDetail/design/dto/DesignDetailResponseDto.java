package com.together.project.ProjectDetail.design.dto;

import com.together.project.ProjectDetail.common.FileMetaDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DesignDetailResponseDto {

    private String type;
    private String text;
    private String json;
    private List<FileMetaDto> files;

    // 일반용
    public DesignDetailResponseDto(String type, String text, List<FileMetaDto> files) {
        this.type = type;
        this.text = text;
        this.files = files;
    }

    // 클래스 다이어그램용
    public DesignDetailResponseDto(String type, String text, String json, List<FileMetaDto> files) {
        this.type = type;
        this.text = text;
        this.json = json;
        this.files = files;
    }
}