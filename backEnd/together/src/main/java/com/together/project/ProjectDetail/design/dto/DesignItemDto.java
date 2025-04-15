package com.together.project.ProjectDetail.design.dto;

import com.together.project.ProjectDetail.common.FileMetaDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class DesignItemDto {

    private String text;
    private String json;
    private List<FileMetaDto> files;

    // ✅ 일반 텍스트 용도
    public DesignItemDto(String text, List<FileMetaDto> files) {
        this.text = text;
        this.files = files;
    }

    // ✅ 클래스 다이어그램용 (json 포함)
    public DesignItemDto(String text, String json, List<FileMetaDto> files) {
        this.text = text;
        this.json = json;
        this.files = files;
    }
}