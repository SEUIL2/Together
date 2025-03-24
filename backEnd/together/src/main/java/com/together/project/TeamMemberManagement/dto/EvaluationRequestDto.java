package com.together.project.TeamMemberManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequestDto {
    private Long evaluatorId;
    private Long evaluateeId;
    private Long projectId;
    private int score;
    private String comment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime evaluationDate;  // ✅ 클라이언트에서 날짜 전송
}
