package com.together.ProjectDetail.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileMetaDto {
    private String url;
    private LocalDateTime uploadedAt;

    // ⭐️ 파일 MIME 타입 정보 추가 (예: image/png, image/jpeg 등)
    private String fileType; // 예: "image/png", "application/pdf" 등
}
