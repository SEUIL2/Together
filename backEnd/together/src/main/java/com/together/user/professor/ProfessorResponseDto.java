package com.together.user.professor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfessorResponseDto {
    private Long userId;
    private String userName;
    private String userEmail;

}
