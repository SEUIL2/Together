package com.together.user.professor.feedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackCategoryDto {
    private Long id; //pk
    private String name; //카테고리 이름
}