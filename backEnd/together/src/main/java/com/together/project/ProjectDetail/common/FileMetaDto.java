package com.together.project.ProjectDetail.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileMetaDto {
    private String url;
    private LocalDateTime uploadedAt;
}
