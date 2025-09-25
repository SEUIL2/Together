package com.together.user.professor.feedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FeedbackTextHistoryDto {
    private Long id;
    private String text;
    private LocalDateTime usedAt;
}