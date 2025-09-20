package com.together.user.professor.feedback.DTO;

import com.together.user.professor.feedback.FeedbackCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackSummaryDto {
    private Long feedbackId;
    private LocalDateTime createdAt;
    private String page;
    private String text;
    private Long authorId;
    private Boolean isRead;
    private FeedbackCategory category;
}
