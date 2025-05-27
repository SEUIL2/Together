package com.together.user.professor.feedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDto {

    private Long feedbackId;
    private Long projectId;
    private String page;
    private int x;
    private int y;
    private String text;
    private String author;
    private LocalDateTime createdAt;
    private Boolean isRead;

}
