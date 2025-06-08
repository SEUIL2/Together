package com.together.generatedCodeAI.DTO;

import com.together.generatedCodeAI.CodeSourceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class GeneratedCodeResponse {
    private Long id;
    private String codeName;
    private String language;
    private String authorName;
    private CodeSourceType type;
    private String generatedCode;
    private LocalDateTime createdAt;
}
