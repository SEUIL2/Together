package com.together.Invitation.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponseDto {
    private Long invitationId;
    private String projectTitle;
    private String userName;
    private String status;
}