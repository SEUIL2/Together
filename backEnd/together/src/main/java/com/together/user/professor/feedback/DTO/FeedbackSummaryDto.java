package com.together.user.professor.feedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

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
    //private FeedbackCategory category;
    private Set<String> categories; // 변경: Set<String>으로 타입 수정

}
