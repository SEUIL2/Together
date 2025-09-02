package com.together.ProjectDetail.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FileMeta {
    private String url;               // 업로드된 파일 URL
    private LocalDateTime uploadedAt; // 업로드된 시간
    // ⭐️ 파일 타입(MIME 타입, 예: image/png, image/jpeg, application/pdf 등)
    private String fileType;
}

