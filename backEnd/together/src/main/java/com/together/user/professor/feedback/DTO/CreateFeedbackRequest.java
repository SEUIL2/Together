package com.together.user.professor.feedback.DTO;

import lombok.Data;

@Data
public class CreateFeedbackRequest {
    private Long projectId;
    private String page;
    private int x;
    private int y;
    private String text;
}