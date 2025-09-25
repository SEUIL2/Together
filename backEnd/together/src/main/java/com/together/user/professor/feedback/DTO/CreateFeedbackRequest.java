package com.together.user.professor.feedback.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CreateFeedbackRequest {
    private Long projectId;
    private String page;
    private int x;
    private int y;
    private String text;
    private Boolean isRead;
    //private FeedbackCategory category; //삭제
    private List<Long> categoryIds; // 변경 및 추가
}