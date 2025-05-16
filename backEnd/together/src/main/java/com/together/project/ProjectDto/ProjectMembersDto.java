package com.together.project.ProjectDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMembersDto {
    private Long userId;
    private String userName;
    private String studentNumber; //교수는 Null
    private String role;
    private boolean isLeader; //팀장 여부
}
