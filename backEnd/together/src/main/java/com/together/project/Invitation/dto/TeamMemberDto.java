package com.together.project.Invitation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamMemberDto {
    private Long userId;         // 사용자 ID
    private String userName;     // 이름
    private String userEmail;    // 이메일
    private String role;      // 역할 (STUDENT / PROFESSOR)
    private String userColor; //유저 색상
}