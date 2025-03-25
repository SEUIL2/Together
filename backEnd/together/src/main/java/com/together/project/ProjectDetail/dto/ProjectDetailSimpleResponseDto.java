package com.together.project.ProjectDetail.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectDetailSimpleResponseDto {
    private String message;
    private String uploadedImageUrl;

    public ProjectDetailSimpleResponseDto(String message, String uploadedImageUrl) {
        this.message = message;
        this.uploadedImageUrl = uploadedImageUrl;
    }
}