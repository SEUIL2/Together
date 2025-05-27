package com.together.project.ProjectDto;

import com.together.project.Invitation.dto.TeamMemberDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 교수의 프로젝트 리스트 응답 DTO
 * 생성일, 제목, 이미지와 함께 팀원 목록 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryWithMembersDto {
    private Long projectId;         // 프로젝트 ID
    private String title;           // 프로젝트 제목
    private String imageUrl;        // 이미지 URL (nullable)
    private Date createdAt;         // 생성일
    private List<TeamMemberDto> members;  // 팀원 리스트 (학생 + 교수)
}
