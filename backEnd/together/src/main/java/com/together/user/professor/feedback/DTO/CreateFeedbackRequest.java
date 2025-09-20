package com.together.user.professor.feedback.DTO;

import com.together.user.professor.feedback.FeedbackCategory;
import lombok.Data;

@Data
public class CreateFeedbackRequest {
    private Long projectId;
    private String page;
    private int x;
    private int y;
    private String text;
    private Boolean isRead;
    private FeedbackCategory category; //카테고리 추가
}