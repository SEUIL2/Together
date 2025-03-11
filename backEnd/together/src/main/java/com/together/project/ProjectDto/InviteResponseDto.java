package com.together.project.ProjectDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponseDto {
    private Long projectId;
    private String invitedUserEmail;
    private String message;
}